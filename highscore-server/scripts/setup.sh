#!/usr/bin/env bash
# Idempotent: re-running creates only what's missing.
# To rotate the DB or auth password on an existing setup, re-run with ROTATE_PASSWORDS=yes.
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
config_file="$script_dir/gcp.env"

if [[ ! -f "$config_file" ]]; then
  echo "Missing $config_file."
  echo "Copy $script_dir/gcp.env.example to $config_file and fill it in."
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

ROTATE="${ROTATE_PASSWORDS:-no}"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  read -rsp "Database password for Cloud SQL user '$DB_USER': " DB_PASSWORD
  echo
fi
if [[ -z "${AUTH_PASSWORD:-}" ]]; then
  read -rsp "App basic-auth password (for user '$AUTH_USERNAME'): " AUTH_PASSWORD
  echo
fi

echo "==> Setting active project to $PROJECT"
gcloud config set project "$PROJECT"

echo "==> Enabling APIs"
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  cloudbuild.googleapis.com

echo "==> Artifact Registry repo '$REPO'"
if gcloud artifacts repositories describe "$REPO" --location="$REGION" >/dev/null 2>&1; then
  echo "    already exists, skipping"
else
  gcloud artifacts repositories create "$REPO" \
    --repository-format=docker \
    --location="$REGION"
fi

echo "==> Cloud SQL instance '$INSTANCE' (takes ~5 min if new)"
if gcloud sql instances describe "$INSTANCE" >/dev/null 2>&1; then
  echo "    already exists, skipping"
else
  gcloud sql instances create "$INSTANCE" \
    --database-version=POSTGRES_17 \
    --tier=db-f1-micro \
    --region="$REGION"
fi

echo "==> Database '$DB'"
if gcloud sql databases describe "$DB" --instance="$INSTANCE" >/dev/null 2>&1; then
  echo "    already exists, skipping"
else
  gcloud sql databases create "$DB" --instance="$INSTANCE"
fi

echo "==> Cloud SQL user '$DB_USER'"
if gcloud sql users describe "$DB_USER" --instance="$INSTANCE" >/dev/null 2>&1; then
  if [[ "$ROTATE" == "yes" ]]; then
    echo "    rotating password (ROTATE_PASSWORDS=yes)"
    gcloud sql users set-password "$DB_USER" \
      --instance="$INSTANCE" \
      --password="$DB_PASSWORD"
  else
    echo "    already exists, skipping (set ROTATE_PASSWORDS=yes to rotate)"
  fi
else
  gcloud sql users create "$DB_USER" \
    --instance="$INSTANCE" \
    --password="$DB_PASSWORD"
fi

upsert_secret() {
  local name="$1" value="$2"
  if gcloud secrets describe "$name" >/dev/null 2>&1; then
    if [[ "$ROTATE" == "yes" ]]; then
      printf '%s' "$value" | gcloud secrets versions add "$name" --data-file=- >/dev/null
      echo "    added new version for '$name' (ROTATE_PASSWORDS=yes)"
    else
      echo "    '$name' exists, skipping (set ROTATE_PASSWORDS=yes to rotate)"
    fi
  else
    printf '%s' "$value" | gcloud secrets create "$name" --data-file=- >/dev/null
    echo "    created secret '$name'"
  fi
}

echo "==> Secrets"
upsert_secret "$DB_PASSWORD_SECRET" "$DB_PASSWORD"
upsert_secret "$AUTH_PASSWORD_SECRET" "$AUTH_PASSWORD"

echo
echo "Setup complete. Run scripts/deploy.sh to build and deploy."
