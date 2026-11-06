#!/usr/bin/env bash
set -euo pipefail
base="${SMOKE_BASE_URL:?Informe a URL da aplicação de teste/homologação}"
[[ "$base" =~ ^https?://[a-zA-Z0-9.:-]+$ ]] || exit 1
for route in / /servicos /profissionais /login /privacidade; do
    code="$(curl --silent --max-time 15 -o /dev/null -w '%{http_code}' "$base$route")"
    [[ "$code" = 200 ]] || { echo "event=smoke_failed route=$route code=$code" >&2; exit 1; }
done
for route in /clientes /solicitacoes; do
    redirect="$(curl --silent --max-time 15 -o /dev/null -w '%{http_code} %{redirect_url}' "$base$route")"
    [[ "$redirect" = "302 $base/login" ]] || { echo "event=smoke_access_failed route=$route" >&2; exit 1; }
done
echo 'event=smoke_passed'
