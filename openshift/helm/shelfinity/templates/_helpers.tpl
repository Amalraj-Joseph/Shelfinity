{{/*
Expand the name of the chart.
*/}}
{{- define "shelfinity.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "shelfinity.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "shelfinity.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "shelfinity.labels" -}}
helm.sh/chart: {{ include "shelfinity.chart" . }}
{{ include "shelfinity.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "shelfinity.selectorLabels" -}}
app.kubernetes.io/name: {{ include "shelfinity.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "shelfinity.serviceAccountName" -}}
{{- if .Values.security.serviceAccount.create }}
{{- default (include "shelfinity.fullname" .) .Values.security.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.security.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Database labels
*/}}
{{- define "shelfinity.database.labels" -}}
{{ include "shelfinity.labels" . }}
app.kubernetes.io/component: database
{{- end }}

{{/*
Database selector labels
*/}}
{{- define "shelfinity.database.selectorLabels" -}}
{{ include "shelfinity.selectorLabels" . }}
app.kubernetes.io/component: database
{{- end }}

{{/*
Keycloak labels
*/}}
{{- define "shelfinity.keycloak.labels" -}}
{{ include "shelfinity.labels" . }}
app.kubernetes.io/component: keycloak
{{- end }}

{{/*
Keycloak selector labels
*/}}
{{- define "shelfinity.keycloak.selectorLabels" -}}
{{ include "shelfinity.selectorLabels" . }}
app.kubernetes.io/component: keycloak
{{- end }}

{{/*
Backend labels
*/}}
{{- define "shelfinity.backend.labels" -}}
{{ include "shelfinity.labels" . }}
app.kubernetes.io/component: backend
{{- end }}

{{/*
Backend selector labels
*/}}
{{- define "shelfinity.backend.selectorLabels" -}}
{{ include "shelfinity.selectorLabels" . }}
app.kubernetes.io/component: backend
{{- end }}

{{/*
Frontend labels
*/}}
{{- define "shelfinity.frontend.labels" -}}
{{ include "shelfinity.labels" . }}
app.kubernetes.io/component: frontend
{{- end }}

{{/*
Frontend selector labels
*/}}
{{- define "shelfinity.frontend.selectorLabels" -}}
{{ include "shelfinity.selectorLabels" . }}
app.kubernetes.io/component: frontend
{{- end }}