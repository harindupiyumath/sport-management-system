#!/usr/bin/env bash
# Starts the Spring Boot backend inside a GitHub Codespace (called by devcontainer.json).
set -euo pipefail
cd "$(dirname "$0")/../backend"

for v in DB_URL DB_USERNAME DB_PASSWORD JWT_SECRET; do
  if [ -z "${!v:-}" ]; then
    echo "ERROR: Codespaces secret $v is not set (GitHub -> Settings -> Codespaces -> Secrets)." >&2
    exit 1
  fi
done

# Make the forwarded port public so the Vercel site can call the API.
if [ -n "${CODESPACE_NAME:-}" ] && command -v gh >/dev/null 2>&1; then
  gh codespace ports visibility 8080:public -c "$CODESPACE_NAME" >/dev/null 2>&1 \
    || echo "NOTE: could not make port 8080 public automatically - set it in the Ports tab."
fi

chmod +x mvnw
echo "Starting backend; public URL: https://${CODESPACE_NAME:-<codespace>}-8080.${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN:-app.github.dev}"
exec ./mvnw -q spring-boot:run
