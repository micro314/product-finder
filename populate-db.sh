#!/usr/bin/env bash

# Import or clear catalog data in an already-running Compose stack.
set -Eeuo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
compose=(docker compose --project-directory "$project_dir" --file "$project_dir/compose.yaml")
database="product_finder"
collection="catalog_products"
mongo_password="${MONGO_PASSWORD:-product-finder}"
data_file="$project_dir/backend/sample-data/catalog_products.json"

usage() {
  cat <<'EOF'
Usage: ./populate-db.sh [json-file]
       ./populate-db.sh --clear

Import the repository sample catalog, or a supplied JSON array, into MongoDB.
The application stack must already be running with ./start.sh.
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if ! command -v docker >/dev/null 2>&1 || ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose v2 is required but is not available." >&2
  exit 1
fi

if [[ "${1:-}" == "--clear" ]]; then
  if [[ $# -ne 1 ]]; then
    echo "--clear does not accept additional arguments." >&2
    exit 2
  fi
  for attempt in {1..30}; do
    if "${compose[@]}" exec -T mongo mongosh --quiet \
        --username product_finder --password "$mongo_password" \
        --authenticationDatabase admin "$database" \
        --eval 'db.runCommand({ping: 1}).ok' >/dev/null 2>&1; then
      break
    fi
    [[ "$attempt" -eq 30 ]] && { echo "MongoDB did not become ready within 60 seconds." >&2; exit 1; }
    sleep 2
  done
  "${compose[@]}" exec -T mongo mongosh --quiet \
    --username product_finder --password "$mongo_password" \
    --authenticationDatabase admin "$database" \
    --eval "db.$collection.deleteMany({})"
  exit 0
fi

if [[ $# -gt 1 ]]; then
  echo "Expected at most one JSON file." >&2
  exit 2
fi
data_file="${1:-$data_file}"
if [[ ! -f "$data_file" ]]; then
  echo "JSON file not found: $data_file" >&2
  exit 1
fi

for attempt in {1..30}; do
  if "${compose[@]}" exec -T mongo mongosh --quiet \
      --username product_finder --password "$mongo_password" \
      --authenticationDatabase admin "$database" \
      --eval 'db.runCommand({ping: 1}).ok' >/dev/null 2>&1; then
    break
  fi
  [[ "$attempt" -eq 30 ]] && { echo "MongoDB did not become ready within 60 seconds." >&2; exit 1; }
  sleep 2
done

"${compose[@]}" exec -T mongo mongoimport \
  --username product_finder --password "$mongo_password" \
  --authenticationDatabase admin --db "$database" \
  --collection "$collection" --type json --jsonArray \
  --mode upsert --upsertFields _id --file - < "$data_file"
