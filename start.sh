#!/usr/bin/env bash

# Build the application images first. Docker Compose starts only if both builds succeed.
set -Eeuo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required but was not found on PATH." >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose v2 is required but is not available." >&2
  exit 1
fi

docker build --tag product-finder-backend:local "$project_dir/backend"
docker build --tag product-finder-frontend:local "$project_dir/frontend"

exec docker compose --project-directory "$project_dir" --file "$project_dir/compose.yaml" up --detach --no-build "$@"
