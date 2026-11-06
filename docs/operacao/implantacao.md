# Implantação, migração e recuperação

Configurações e scripts preparados localmente não são evidência de produção implantada.

## Preparação do servidor Linux

1. Designar operador, servidor e domínio. Instalar Docker Engine/Compose, Python 3, curl, flock, OpenSSL, coreutils (timeout), age e canal de recebimento de falhas de cron/deploy. Custos e disponibilidade dependem do ambiente escolhido.
2. Instalar os arquivos versionados `scripts/` e `deploy/` em `/srv/precisei`. O administrador deve ser proprietário dos scripts/configurações; a chave SSH de deploy não pode alterá-los.
3. Copiar `deploy/runtime.env.example` para `/srv/precisei/runtime.env`, preencher e restringir leitura. Esse arquivo é **configuração confiável executada por source**; nunca aceitar edição por usuário da aplicação/PR.
4. Preparar MySQL sem acesso público. Contas distintas: aplicação com DML no esquema, sem DDL/administração; migração com DDL/DML somente nesse esquema; backup com privilégios mínimos verificados por exercício real.
5. Resolver proxy e cliente MySQL por digest (`docker pull` e `docker image inspect`); preencher domínio, banco e hash de manutenção. O servidor deve poder ler o registro de imagens.
6. Gerar chave age de recuperação fora da aplicação. Colocar somente a chave **pública** em `AGE_RECIPIENT`. Preservar a privada separada e configurar cópia externa de backup; pasta local no mesmo disco não é cópia independente.

Não dar acesso Docker ao usuário da aplicação. Em `authorized_keys`, chave do pipeline deve ter comando fixo, por exemplo `command="/usr/bin/bash /srv/precisei/scripts/deploy.sh",restrict`. Fixar chave conhecida do host; nunca usar `StrictHostKeyChecking=no`. O comando recebe somente manifesto JSON e não interpreta comandos enviados pelo cliente.

## Linha de origem do banco

Banco operacional novo: `DB_SCHEMA_LINEAGE=production`, V1/V2 em `db/production` sem contas demonstrativas + V3 em `db/common`. Banco histórico: `DB_SCHEMA_LINEAGE=migration`, V1/V2 originais + V3 comum. Não trocar linha de origem de uma base existente.

O job `migrate` executa o mesmo JAR com perfil `migrate`, sem servidor HTTP, e encerra após sucesso. O processo `prod` usa conta restrita e não executa Flyway. Em base histórica, contas demonstrativas exigem reconciliação autorizada; o guard de produção bloqueia sua presença sem excluí-las.

Antes de migrar: conferir espaço, backup e compatibilidade. Não reescrever migrações aplicadas ou usar `repair` para contornar checksum. DDL do MySQL não tem garantia de rollback transacional completo.

## Ativação do GitHub

Esta implementação não fez push nem publicou imagem. Antes de habilitar CD:

- Publicar/revisar a branch, proteger a integração e configurar ambientes `staging` e `production`.
- Produção deve ter aprovador obrigatório e branch restrita. O script consulta a proteção e recusa a promoção quando faltar.
- Secrets por ambiente: `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_KEY` e `DEPLOY_KNOWN_HOSTS`.
- Confirmar acesso ao GHCR e orçamento de minutos/armazenamento. PR externo não recebe credenciais de deploy.
- `workflow_run` exige que CD esteja disponível na branch padrão; verificar isso ao ativar.

CI guarda a imagem que executou o E2E. Publicação confere ID/checksum do JAR e promove sem recompilar. CD valida origem push, workflow, repositório, branch, SHA, manifesto e digest. Produção exige informar os IDs da CI e da CD de homologação bem-sucedidas; confere a origem da homologação e a igualdade dos manifestos/digests antes de solicitar implantação no ambiente protegido.

## Falha e retorno

Deploy usa flock, backup, migração, atualização e observação da saúde. Pode haver breve interrupção e novo login (sessões em memória). Não promete zero downtime.
Imagem anterior só retorna se aceitar o esquema atual. Não repetir migração falha cegamente. Sem retorno seguro, manter manutenção e abrir incidente. Restore não é automático sobre produção.

## Exercício de restauração

Criar uma base vazia terminada em `_restore_test`. Disponibilizar temporariamente a chave privada de recuperação no host de exercício e confirmar explicitamente o nome:

```bash
export RESTORE_DATABASE=precisei_restore_test
export CONFIRM_RESTORE_DATABASE=precisei_restore_test
export AGE_IDENTITY=/local-seguro/recuperacao.key
bash scripts/restore.sh /srv/precisei/backups/precisei-DATA-ID.sql.age
```

O script confere checksum, recusa base com tabelas e não aceita o banco operacional. Após o restore, comparar contagens/vínculos, conferir histórico Flyway e executar fumaça em aplicação separada. RPO de 24h/RTO de 4h continuam metas do piloto até medição.

## Monitoramento e retenção

Instalar o cron somente após exercício e configuração do canal. `monitor.sh` verifica prontidão, HTTPS, certificado com mais de sete dias e backup com menos de 26h; saída de erro precisa chegar a uma pessoa responsável. A confirmação de implantação e de retorno executa também `smoke.sh`, incluindo a recusa de acesso anônimo a clientes/pedidos.
O processo web de produção expõe gestão na porta 9090, publicada somente em loopback/rede privada; o proxy bloqueia `/actuator` ao público. O scrape Prometheus é permitido apenas nesse conector privado. Configurações `deploy/prometheus.yml` e `deploy/alerts.yml` preparam coleta e alertas de disponibilidade/5xx/latência; o operador precisa instalar o coletor em rede privada, configurar Alertmanager/canal, armazenamento e validar disparos. Demais métricas da aplicação exigem manutenção autenticada.
Backup é criptografado, copiado e comparado antes do expurgo local antigo. Destino externo deve ser fisicamente independente. Retenção remota, restore trimestral e limpeza de imagens exigem revisão operacional; não apagar imagem em uso ou necessária ao retorno.

## Validação e limites

Antes de usar: `pnpm check`, `python -m unittest discover -s tests/ops -v`, `bash -n scripts/*.sh` (ou percorrer os arquivos), `docker compose --env-file runtime.env -f deploy/compose.prod.yml config --quiet` e exercício em staging.
DNS/TLS, credenciais, aprovadores, alertas, infraestrutura e aceite humano não podem ser presumidos pelo código. MySQL local isolado verifica Java/SQL, mas não substitui teste real de contêiner/implantação.
