CREATE TABLE IF NOT EXISTS vaults (
  vault_id TEXT PRIMARY KEY,
  secret_hash TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  last_seen_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS backups (
  vault_id TEXT PRIMARY KEY,
  backup_id TEXT NOT NULL,
  object_key TEXT NOT NULL,
  byte_size INTEGER NOT NULL,
  checksum TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  FOREIGN KEY (vault_id) REFERENCES vaults(vault_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_backups_created_at ON backups(created_at DESC);
