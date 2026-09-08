# StickHub Cloud Backup Worker

This Worker stores only client-side encrypted `.stickhub` archives. It keeps
vault metadata in D1 and the encrypted blob in the existing private R2
account through the S3-compatible API.

## Required secrets

Set these with `wrangler secret put`; never commit them:

- `R2_ENDPOINT`
- `R2_BUCKET`
- `R2_ACCESS_KEY_ID`
- `R2_SECRET_ACCESS_KEY`

The Worker account uses D1 for the vault index. The R2 credentials are only
used server-side and are never shipped in the APK.

## Deploy

```powershell
npm install
wrangler d1 migrations apply stickhub-cloud --remote
wrangler secret put R2_ENDPOINT
wrangler secret put R2_BUCKET
wrangler secret put R2_ACCESS_KEY_ID
wrangler secret put R2_SECRET_ACCESS_KEY
wrangler deploy
```

The current production endpoint is configured in the Android client as
`https://stickhub-cloud.cloud-backup-worker.workers.dev`.
