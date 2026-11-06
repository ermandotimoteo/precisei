#!/usr/bin/env bash
set -euo pipefail
root="${DEPLOY_ROOT:-/srv/precisei}"
cd "$root"
set -a; source runtime.env; set +a
: "${RESTORE_DATABASE:?Informe uma base nova terminada em _restore_test}"
: "${CONFIRM_RESTORE_DATABASE:?Confirme explicitamente a base de destino}"
: "${AGE_IDENTITY:?Informe a chave privada age fora do repositório}"
[[ "$RESTORE_DATABASE" =~ ^[a-zA-Z0-9_]+_restore_test$ && "$RESTORE_DATABASE" = "$CONFIRM_RESTORE_DATABASE" && "$RESTORE_DATABASE" != "$DB_NAME" ]] || exit 1
file="$(realpath "${1:?Informe o backup criptografado}")"
[[ "$file" == "$root/backups/precisei-"*.sql.age ]] || { echo 'Backup fora do diretório permitido.' >&2; exit 1; }
test -f "$file.sha256"
sha256sum --check "$file.sha256"
tables="$(printf 'SHOW TABLES;\n' | docker compose --env-file runtime.env -f deploy/compose.prod.yml run --rm -T --no-deps restore)"
[[ -z "$tables" ]] || { echo 'A base de restauração deve estar vazia.' >&2; exit 1; }
age -d -i "$AGE_IDENTITY" "$file" | docker compose --env-file runtime.env -f deploy/compose.prod.yml run --rm -T --no-deps restore
printf 'SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1;\n' | docker compose --env-file runtime.env -f deploy/compose.prod.yml run --rm -T --no-deps restore
echo 'event=restore_completed (validar contagens/vínculos antes de qualquer promoção manual)'
