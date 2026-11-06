#!/usr/bin/env bash
set -euo pipefail
: "${DEPLOY_HOST:?}" "${DEPLOY_USER:?}" "${DEPLOY_KEY:?}" "${DEPLOY_KNOWN_HOSTS:?}"
[[ "$DEPLOY_HOST" =~ ^[a-zA-Z0-9.-]+$ && "$DEPLOY_USER" =~ ^[a-zA-Z0-9_-]+$ ]] || exit 1
umask 077
dir="$(mktemp -d)"
trap 'rm -f -- "$dir/key" "$dir/known_hosts"; rmdir "$dir"' EXIT
printf '%s\n' "$DEPLOY_KEY" > "$dir/key"
printf '%s\n' "$DEPLOY_KNOWN_HOSTS" > "$dir/known_hosts"
# A chave deve usar command=".../deploy.sh",restrict em authorized_keys no servidor.
ssh -i "$dir/key" -o BatchMode=yes -o StrictHostKeyChecking=yes -o "UserKnownHostsFile=$dir/known_hosts" \
    "$DEPLOY_USER@$DEPLOY_HOST" < "${1:?Informe o manifesto validado}"
