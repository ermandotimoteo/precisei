#!/usr/bin/env bash
set -euo pipefail
root="${DEPLOY_ROOT:-/srv/precisei}"
cd "$root"
set -a; source runtime.env; set +a
curl --fail --silent --max-time 10 "http://127.0.0.1:${MANAGEMENT_PORT:-9090}/actuator/health/readiness" >/dev/null
curl --fail --silent --max-time 10 "https://$PUBLIC_HOST/" >/dev/null
command -v openssl >/dev/null
printf '\n' | timeout 15 openssl s_client -connect "$PUBLIC_HOST:443" -servername "$PUBLIC_HOST" 2>/dev/null |
    openssl x509 -noout -checkend 604800 >/dev/null || { echo 'alert=tls_certificate_less_than_7_days_or_unavailable' >&2; exit 1; }
latest="$(find "$root/backups" -maxdepth 1 -type f -name 'precisei-*.sql.age' -printf '%T@\n' | sort -nr | head -1)"
[[ -n "$latest" ]] || { echo 'alert=backup_missing' >&2; exit 1; }
latest="${latest%%.*}"
(( $(date +%s) - latest < 93600 )) || { echo 'alert=backup_older_than_26h' >&2; exit 1; }
echo 'event=monitor_ok'
