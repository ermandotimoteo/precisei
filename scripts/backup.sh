#!/usr/bin/env bash
set -euo pipefail
root="${DEPLOY_ROOT:-/srv/precisei}"
[[ "$root" = /* && "$root" != / ]] || exit 1
cd "$root"
set -a; source runtime.env; set +a
: "${AGE_RECIPIENT:?Configure uma chave pública age}"
dir="${BACKUP_DIR:-$root/backups}"
[[ "$dir" = "$root/backups" ]] || { echo 'Diretório de backup inesperado.' >&2; exit 1; }
command -v age >/dev/null
mkdir -p "$dir"
umask 077
temp="$(mktemp "$dir/.backup.XXXXXX")"
trap 'rm -f -- "$temp"' EXIT
docker compose --env-file runtime.env -f deploy/compose.prod.yml run --rm -T --no-deps backup |
    age -r "$AGE_RECIPIENT" > "$temp"
test -s "$temp"
file="$dir/precisei-$(date -u +%Y%m%dT%H%M%S)-$RANDOM.sql.age"
mv "$temp" "$file"
sha256sum "$file" > "$file.sha256"
# A chave privada de restauração deve ficar separada do servidor da aplicação.
if [[ -n "${BACKUP_OFFSITE_DIR:-}" ]]; then
    [[ "$BACKUP_OFFSITE_DIR" = /* && "$BACKUP_OFFSITE_DIR" != "$dir" && -d "$BACKUP_OFFSITE_DIR" ]] || exit 1
    cp -- "$file" "$file.sha256" "$BACKUP_OFFSITE_DIR/"
    cmp -- "$file" "$BACKUP_OFFSITE_DIR/$(basename "$file")"
    # Só habilitar expurgo após cópia externa verificada; nunca recursivo.
    find "$dir" -maxdepth 1 -type f -name 'precisei-*.sql.age*' -mtime "+${BACKUP_RETENTION_DAYS:-7}" -delete
fi
printf 'event=backup_completed file=%s\n' "$(basename "$file")"
