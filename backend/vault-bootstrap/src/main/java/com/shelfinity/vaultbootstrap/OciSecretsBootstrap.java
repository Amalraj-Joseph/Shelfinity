/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.vaultbootstrap;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * Cloud-only startup step (see this module's pom.xml for why it's a
 * separate project, not a dependency of the backend WAR): fetches
 * Shelfinity's secrets from OCI Vault and writes them as a plain
 * KEY=value file for systemd's EnvironmentFile= to load before the
 * backend process starts.
 *
 * Auth is Instance Principals — the VM authenticates as itself via its own
 * instance metadata, no API key or credential file ever lives on disk (see
 * the runbook's Vault section for the dynamic-group/policy this requires).
 * That mechanism only works when actually running on an OCI compute
 * instance; it cannot be exercised from a local dev machine.
 *
 * Usage: java -jar vault-bootstrap.jar /etc/shelfinity/vault.env
 *
 * Required env vars (the three secret OCIDs — not secrets themselves, just
 * Vault resource identifiers, safe to set directly in the systemd unit):
 *   VAULT_DB2_CREDENTIALS_OCID
 *   VAULT_APPID_CONFIG_OCID
 *   VAULT_APP_SECRETS_OCID
 */
public class OciSecretsBootstrap {

    // secret env var (holding the OCID) -> ordered list of (json key -> output env var name)
    private static final Map<String, List<KeyMapping>> SECRETS = new LinkedHashMap<>();
    static {
        SECRETS.put("VAULT_DB2_CREDENTIALS_OCID", List.of(
                new KeyMapping("jdbcUrl", "DB_JDBC_URL"),
                new KeyMapping("driverClass", "DB_DRIVER_CLASS"),
                new KeyMapping("user", "DB_USER"),
                new KeyMapping("password", "DB_PASSWORD")
        ));
        SECRETS.put("VAULT_APPID_CONFIG_OCID", List.of(
                new KeyMapping("issuer", "OIDC_ISSUER"),
                new KeyMapping("jwksUri", "OIDC_JWKS_URI"),
                new KeyMapping("tokenEndpoint", "OIDC_TOKEN_ENDPOINT"),
                new KeyMapping("userinfoEndpoint", "OIDC_USERINFO_ENDPOINT"),
                // OIDC_AUDIENCE isn't its own field in the secret — App ID's
                // audience claim is the client ID, so the same value is
                // written under both env var names rather than duplicating
                // it as a second field in Vault.
                new KeyMapping("clientId", "OIDC_CLIENT_ID"),
                new KeyMapping("clientId", "OIDC_AUDIENCE")
        ));
        SECRETS.put("VAULT_APP_SECRETS_OCID", List.of(
                new KeyMapping("emailConfigEncryptionKey", "EMAIL_CONFIG_ENCRYPTION_KEY")
        ));
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar vault-bootstrap.jar <output-env-file-path>");
            System.exit(2);
        }
        Path outputPath = Path.of(args[0]);

        // Validated up front, before anything OCI-related: building the
        // Instance Principals provider itself makes a network call to the
        // instance metadata service to auto-detect identity/region. Off an
        // actual OCI VM (or if these env vars are simply wrong), that call
        // hangs on the default client timeout instead of failing fast — so
        // a misconfigured systemd unit would look "stuck starting" rather
        // than cleanly erroring. Checking config first, before any network
        // I/O, was found the hard way testing this locally.
        Map<String, String> secretOcids = new LinkedHashMap<>();
        try {
            for (String secretOcidEnvVar : SECRETS.keySet()) {
                secretOcids.put(secretOcidEnvVar, requireEnv(secretOcidEnvVar));
            }
        } catch (IllegalStateException e) {
            System.err.println("FATAL: " + e.getMessage());
            System.exit(1);
        }

        Map<String, String> resolvedEnv = new LinkedHashMap<>();
        try {
            InstancePrincipalsAuthenticationDetailsProvider authProvider =
                    InstancePrincipalsAuthenticationDetailsProvider.builder().build();
            try (SecretsClient secretsClient = SecretsClient.builder().build(authProvider)) {
                for (Map.Entry<String, List<KeyMapping>> entry : SECRETS.entrySet()) {
                    String secretOcid = secretOcids.get(entry.getKey());
                    JsonObject secretJson = fetchSecretJson(secretsClient, secretOcid);
                    for (KeyMapping mapping : entry.getValue()) {
                        String value = secretJson.getString(mapping.jsonKey, null);
                        if (value == null) {
                            throw new IllegalStateException("Secret " + entry.getKey()
                                    + " is missing expected field \"" + mapping.jsonKey + "\"");
                        }
                        resolvedEnv.put(mapping.envVar, value);
                    }
                }
            }
        } catch (Exception e) {
            // Deliberately loud on stderr, not just a stack trace: this runs
            // unattended under systemd, and whoever's debugging a failed
            // start will be reading `journalctl -u shelfinity-backend`, not
            // stepping through a debugger.
            System.err.println("FATAL: could not resolve secrets from OCI Vault: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        try {
            writeEnvFile(outputPath, resolvedEnv);
        } catch (IOException e) {
            System.err.println("FATAL: could not write " + outputPath + ": " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Wrote " + resolvedEnv.size() + " variables to " + outputPath);
    }

    private static JsonObject fetchSecretJson(SecretsClient secretsClient, String secretOcid) {
        GetSecretBundleResponse response = secretsClient.getSecretBundle(
                GetSecretBundleRequest.builder()
                        .secretId(secretOcid)
                        .stage(GetSecretBundleRequest.Stage.Current)
                        .build());
        SecretBundleContentDetails content = response.getSecretBundle().getSecretBundleContent();
        if (!(content instanceof Base64SecretBundleContentDetails base64Content)) {
            throw new IllegalStateException("Secret " + secretOcid + " is not base64 content — unexpected bundle type");
        }
        String decoded = new String(
                Base64.getDecoder().decode(base64Content.getContent()), StandardCharsets.UTF_8);
        return Json.createReader(new StringReader(decoded)).readObject();
    }

    /**
     * Plain KEY=value, one per line — the format systemd's EnvironmentFile=
     * expects. No shell quoting/export syntax; this is read by systemd
     * itself, not sourced by a shell.
     */
    private static void writeEnvFile(Path outputPath, Map<String, String> env) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : env.entrySet()) {
            sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        }
        Files.createDirectories(outputPath.toAbsolutePath().getParent());
        Files.writeString(outputPath, sb.toString(), StandardCharsets.UTF_8);
        // Contains real secrets (Db2 password, the email encryption key) —
        // owner-read/write only.
        try {
            Files.setPosixFilePermissions(outputPath,
                    PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignored) {
            // Non-POSIX filesystem (shouldn't happen on the target Linux VM,
            // but don't fail the whole bootstrap over a chmod on a platform
            // that doesn't support it).
        }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable " + name + " is not set");
        }
        return value;
    }

    private record KeyMapping(String jsonKey, String envVar) {
    }
}
