#!/usr/bin/env bash
set -euo pipefail
root="${DEPLOY_ROOT:-/srv/precisei}"
[[ "$root" = /* && "$root" != / ]] || exit 1
cd "$root"
set -a; source runtime.env; set +a
mkdir -p state
exec 9>state/deploy.lock
flock -n 9 || { echo 'Já existe uma implantação em andamento.' >&2; exit 1; }
umask 077
candidate="$(mktemp "$root/state/candidate.XXXXXX")"
trap 'rm -f -- "$candidate"' EXIT
cat > "$candidate"
export IMAGE
IMAGE="$(python3 scripts/ops.py image "$candidate")"
for image in "$IMAGE" "$CADDY_IMAGE" "$MYSQL_CLIENT_IMAGE"; do
    [[ "$image" =~ @sha256:[a-f0-9]{64}$ ]] || { echo 'Imagem sem digest.' >&2; exit 1; }
done
compose() { docker compose --env-file runtime.env -f deploy/compose.prod.yml "$@"; }
df -Pk "$root" | awk 'NR==2 {if ($4 < 1048576) exit 1}'
docker pull "$IMAGE"
actual="$(docker image inspect --format '{{index .Config.Labels "org.opencontainers.image.revision"}}' "$IMAGE")"
[[ "$actual" = "$(python3 scripts/ops.py commit "$candidate")" ]] || { echo 'Origem da imagem não confere.' >&2; exit 1; }
bash scripts/backup.sh
# A migração é uma etapa própria. Nunca cancelar job enquanto ela está em curso.
compose run --rm -T --no-deps migrate
schema="$(printf 'SELECT COALESCE(MAX(CAST(version AS UNSIGNED)),0) FROM flyway_schema_history WHERE success = 1;\n' | compose run --rm -T --no-deps query)"
python3 scripts/ops.py compatible "$candidate" "$schema"
compose up -d --no-deps app proxy
healthy() { curl --silent --fail --max-time 5 "http://127.0.0.1:${MANAGEMENT_PORT:-9090}/actuator/health/readiness" >/dev/null; }
ready=0
for attempt in $(seq 1 36); do if healthy; then ready=1; break; fi; sleep 5; done
if [[ "$ready" = 1 ]]; then
    duration="${OBSERVE_SECONDS:-900}"
    [[ "$duration" =~ ^[0-9]+$ ]] || exit 1
    if [[ "${DEPLOY_ENV:-staging}" = production && "$duration" -lt 900 ]]; then exit 1; fi
    deadline=$((SECONDS + duration)); failures=0
    while (( SECONDS < deadline )); do
        if healthy; then failures=0; else failures=$((failures + 1)); fi
        if (( failures >= 3 )); then ready=0; break; fi
        sleep 10
    done
fi
if [[ "$ready" = 1 ]] && SMOKE_BASE_URL="https://$PUBLIC_HOST" bash scripts/smoke.sh; then
    cp -- "$candidate" state/current-release.json
    echo "event=deploy_completed image=$IMAGE schema=$schema"
    exit 0
fi
if [[ -f state/current-release.json ]] && python3 scripts/ops.py compatible state/current-release.json "$schema"; then
    IMAGE="$(python3 scripts/ops.py image state/current-release.json)"; export IMAGE
    compose up -d --no-deps app
    recovered=0
    for attempt in $(seq 1 36); do if healthy; then recovered=1; break; fi; sleep 5; done
    if [[ "$recovered" = 1 ]] && SMOKE_BASE_URL="https://$PUBLIC_HOST" bash scripts/smoke.sh; then
        echo 'event=deploy_failed rollback_healthy (abrir incidente)' >&2
    else
        compose stop app
        echo 'event=deploy_failed maintenance_required (retorno sem saúde)' >&2
    fi
else
    compose stop app
    echo 'event=deploy_failed maintenance_required (sem retorno seguro)' >&2
fi
exit 1
