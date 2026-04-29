#!/usr/bin/env bash
# Run scripts/setup.sh once first to provision the surrounding infrastructure.
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(cd "$script_dir/.." && pwd)"
config_file="$script_dir/gcp.env"

if [[ ! -f "$config_file" ]]; then
  echo "Missing $config_file. Run scripts/setup.sh first."
  exit 1
fi
# shellcheck source=/dev/null
source "$config_file"

required=(PROJECT REGION INSTANCE DB DB_USER REPO SERVICE AUTH_USERNAME DB_PASSWORD_SECRET AUTH_PASSWORD_SECRET)
for v in "${required[@]}"; do
  if [[ -z "${!v:-}" ]]; then
    echo "Missing variable in gcp.env: $v"
    exit 1
  fi
done

IMAGE="$REGION-docker.pkg.dev/$PROJECT/$REPO/server:latest"
INSTANCE_CONN="$PROJECT:$REGION:$INSTANCE"
JDBC_URL="jdbc:postgresql:///$DB?cloudSqlInstance=$INSTANCE_CONN&socketFactory=com.google.cloud.sql.postgres.SocketFactory"

echo "==> Building $IMAGE via Cloud Build"
( cd "$project_root" && gcloud builds submit --tag "$IMAGE" )

echo "==> Deploying Cloud Run service '$SERVICE'"
gcloud run deploy "$SERVICE" \
  --image="$IMAGE" \
  --region="$REGION" \
  --add-cloudsql-instances="$INSTANCE_CONN" \
  --set-env-vars="^@^POSTGRES_URL=$JDBC_URL@POSTGRES_USER=$DB_USER@AUTH_USERNAME=$AUTH_USERNAME" \
  --set-secrets="POSTGRES_PASSWORD=$DB_PASSWORD_SECRET:latest,AUTH_PASSWORD=$AUTH_PASSWORD_SECRET:latest" \
  --allow-unauthenticated

echo
url="$(gcloud run services describe "$SERVICE" --region="$REGION" --format='value(status.url)')"
echo "Deployed: $url"
