#!/usr/bin/env bash
set -euo pipefail

# One-click deploy on server
# Usage:
#   bash deploy-server.sh [archive_name] [target_dir] [archive_dir]
# Example:
#   bash deploy-server.sh timemark.tar.gz /home/ubuntu/timemark /home/ubuntu

ARCHIVE_NAME="${1:-timemark.tar.gz}"
TARGET_DIR="${2:-/home/ubuntu/timemark}"
ARCHIVE_DIR="${3:-/home/ubuntu}"
ARCHIVE_PATH="${ARCHIVE_DIR}/${ARCHIVE_NAME}"

if ! command -v docker >/dev/null 2>&1; then
  echo "[ERROR] docker is not installed"
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[ERROR] docker compose plugin is not available"
  exit 1
fi

if [[ ! -f "$ARCHIVE_PATH" ]]; then
  echo "[ERROR] Archive not found: $ARCHIVE_PATH"
  echo "        Please upload it first, e.g.: scp timemark.tar.gz user@server:${ARCHIVE_DIR}/"
  exit 1
fi

echo "[1/6] Preparing target directory: $TARGET_DIR"
mkdir -p "$TARGET_DIR"

echo "[2/6] Cleaning old files (keeping .env if exists)"
if [[ -f "$TARGET_DIR/.env" ]]; then
  find "$TARGET_DIR" -mindepth 1 -maxdepth 1 ! -name '.env' -exec rm -rf {} +
else
  rm -rf "$TARGET_DIR"/*
fi

echo "[3/6] Extracting archive: $ARCHIVE_PATH"
# Extract into a temp directory first to avoid tar metadata errors on the target dir,
# then copy the project contents into the deploy directory.
TMP_EXTRACT_DIR="$(mktemp -d "${ARCHIVE_DIR}/timemark-extract.XXXXXX")"
cleanup() {
  rm -rf "$TMP_EXTRACT_DIR"
}
trap cleanup EXIT

tar -xzf "$ARCHIVE_PATH" -C "$TMP_EXTRACT_DIR"
cp -r "$TMP_EXTRACT_DIR"/. "$TARGET_DIR"/

cd "$TARGET_DIR"

echo "[4/6] Building frontend dist explicitly"
docker compose run --rm frontend-builder

if [[ ! -f "$TARGET_DIR/frontend/dist/index.html" ]]; then
  echo "[ERROR] frontend build did not produce frontend/dist/index.html"
  exit 1
fi

echo "[5/6] Starting backend and recreating nginx"
docker compose up -d --build backend
docker compose rm -sf nginx >/dev/null 2>&1 || true
docker compose up -d nginx

echo "[6/6] Done"
docker compose ps

echo "\nQuick checks:"
echo "  docker compose exec nginx ls -lah /usr/share/nginx/html"
echo "  curl -k -I https://127.0.0.1/"
echo "  curl -k -I https://127.0.0.1/api/health"
echo "\nLogs (optional):"
echo "  docker compose logs -f backend"
echo "  docker compose logs -f nginx"
