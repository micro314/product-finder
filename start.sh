#!/usr/bin/env bash

# Build the application images first. Docker Compose starts only if both builds succeed.
set -Eeuo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
compose=(docker compose --project-directory "$project_dir" --file "$project_dir/compose.yaml")
database="product_finder"
collection="catalog_products"
mongo_password="${MONGO_PASSWORD:-product-finder}"
default_data_file="$project_dir/backend/sample-data/catalog_products.json"

usage() {
  cat <<'EOF'
Usage: ./start.sh [--import-test-data [json-file] | --clear-test-data] [docker-compose-up-options]

Options:
  --import-test-data [json-file]  Start Compose and upsert a JSON array into
                                  catalog_products. The repository sample data is
                                  used when json-file is omitted.
  --clear-test-data               Start Compose and clear catalog_products only.

All remaining arguments are passed to `docker compose up`. Data options start the
stack detached so MongoDB can be prepared before this script exits.
EOF
}

data_action=""
data_file=""
compose_args=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    --import-test-data)
      if [[ -n "$data_action" ]]; then
        echo "Only one test-data action may be specified." >&2
        exit 2
      fi
      data_action="import"
      if [[ $# -gt 1 && "$2" != -* ]]; then
        data_file="$2"
        shift
      fi
      ;;
    --clear-test-data)
      if [[ -n "$data_action" ]]; then
        echo "Only one test-data action may be specified." >&2
        exit 2
      fi
      data_action="clear"
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      compose_args+=("$1")
      ;;
  esac
  shift
done

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

if [[ -z "$data_action" ]]; then
  exec "${compose[@]}" up --no-build "${compose_args[@]}"
fi

"${compose[@]}" up --detach --no-build "${compose_args[@]}"

for attempt in {1..30}; do
  if "${compose[@]}" exec -T mongo mongosh --quiet \
      --username product_finder \
      --password "$mongo_password" \
      --authenticationDatabase admin \
      "$database" \
      --eval 'db.runCommand({ping: 1}).ok' >/dev/null 2>&1; then
    break
  fi

  if [[ "$attempt" -eq 30 ]]; then
    echo "MongoDB did not become ready within 60 seconds." >&2
    exit 1
  fi
  sleep 2
done

if [[ "$data_action" == "clear" ]]; then
  "${compose[@]}" exec -T mongo mongosh \
    --quiet \
    --username product_finder \
    --password "$mongo_password" \
    --authenticationDatabase admin \
    "$database" \
    --eval "db.$collection.deleteMany({})"
  exit 0
fi

data_file="${data_file:-$default_data_file}"
if [[ ! -f "$data_file" ]]; then
  echo "JSON file not found: $data_file" >&2
  exit 1
fi

container_file="/tmp/product-finder-catalog-products-$$.json"
cleanup() {
  "${compose[@]}" exec -T mongo rm -f "$container_file" >/dev/null 2>&1 || true
}
trap cleanup EXIT

"${compose[@]}" cp "$data_file" "mongo:$container_file"
"${compose[@]}" exec -T mongo mongoimport \
  --username product_finder \
  --password "$mongo_password" \
  --authenticationDatabase admin \
  --db "$database" \
  --collection "$collection" \
  --type json \
  --jsonArray \
  --mode upsert \
  --upsertFields _id \
  --file "$container_file"
