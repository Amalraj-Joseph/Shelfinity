# Rebuild from Local Source

After making code changes to your local files, use these commands to rebuild and redeploy.

## Quick Rebuild Commands

### Rebuild Backend Only
```bash
cd /Users/amalrajjoseph/Shadow-Codex/Shelfinity
oc start-build backend -n shelfinity-dev --from-dir=./backend --follow
```

### Rebuild Frontend Only
```bash
cd /Users/amalrajjoseph/Shadow-Codex/Shelfinity
oc start-build frontend -n shelfinity-dev --from-dir=./frontend --follow
```

### Rebuild Both
```bash
cd /Users/amalrajjoseph/Shadow-Codex/Shelfinity

# Backend
oc start-build backend -n shelfinity-dev --from-dir=./backend --follow

# Frontend
oc start-build frontend -n shelfinity-dev --from-dir=./frontend --follow
```

## What Happens During Rebuild

1. **Source Upload**: Your local directory is compressed and uploaded to OpenShift
2. **Build Process**: OpenShift builds a new container image using your Dockerfile
3. **Image Push**: New image is pushed to the internal registry
4. **Auto-Deploy**: Deployment automatically rolls out the new image

## Monitor Build Progress

```bash
# Watch build logs
oc logs -f bc/backend -n shelfinity-dev
oc logs -f bc/frontend -n shelfinity-dev

# Check build status
oc get builds -n shelfinity-dev

# Watch pods rolling out
oc get pods -n shelfinity-dev -w
```

## Troubleshooting

### Build Failed
```bash
# Check build logs
oc logs build/backend-2 -n shelfinity-dev
oc logs build/frontend-2 -n shelfinity-dev

# Describe build for more details
oc describe build/backend-2 -n shelfinity-dev
```

### Pod Not Starting
```bash
# Check pod logs
oc logs deployment/backend -n shelfinity-dev
oc logs deployment/frontend -n shelfinity-dev

# Describe pod for events
oc describe pod <pod-name> -n shelfinity-dev
```

## Development Workflow

1. **Make code changes** in your local files
2. **Test locally** (optional)
3. **Rebuild** using commands above
4. **Verify** the changes in OpenShift
5. **Repeat** as needed

## Tips

- Use `--follow` flag to watch build logs in real-time
- Builds are incremental - only changed layers are rebuilt
- Each build creates a new image tag (backend-1, backend-2, etc.)
- Old images are kept for rollback if needed

## Rollback to Previous Version

```bash
# List recent builds
oc get builds -n shelfinity-dev

# Rollback to previous deployment
oc rollout undo deployment/backend -n shelfinity-dev
oc rollout undo deployment/frontend -n shelfinity-dev
```

# Made with Bob