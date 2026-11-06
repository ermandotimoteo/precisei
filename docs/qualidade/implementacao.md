# Implementação dos planos de qualidade, segurança e CI/CD

Verificação local: **06/11/2026**. Branch de trabalho: `codex/evolucao-qualidade-seguranca`.
Referência anterior: `modulo-3`, commit `469e4d3`. Os quatro PDFs em `output/pdf` permanecem como registros históricos do planejamento.

Commits locais desta evolução: `5a017ec` (aplicação, segurança, ambientes e testes Java) e `a453eda` (automação, testes complementares e operação). A documentação é registrada separadamente. Não houve push.

**Implementado** significa que existe código/configuração; **verificado localmente** indica execução com evidência. Configuração de workflow não significa execução no GitHub, e script de implantação não significa produção implantada.

## Resultado funcional

- Login distingue cliente, profissional e manutenção, inclusive quando os IDs numéricos coincidem.
- Titular do pedido vem da sessão, não do formulário. Listagens e alterações conferem papel e vínculo.
- Cliente solicita, cancela quando permitido e avalia sua conclusão; profissional atribuído aceita, inicia e conclui.
- Catálogo é público; escrita exige manutenção. Cadastro público não cria privilégios administrativos.
- Cadastro/perfil validam dados, senha e associações. Alterar senha profissional exige a senha atual e expira sessões; e-mail original é preservado até existir confirmação de alteração.
- Argon2id protege novas senhas; hashes BCrypt legados são verificados e atualizados após login válido. Não foi aplicada conversão cega de hashes.
- Cookies, sessão, CSRF, CSP, limite de tentativas e erros sem detalhes internos protegem a navegação.
- Contatos de atendimento aparecem somente para participantes/manutenção; há página de privacidade.
- Transações, UNIQUE e lock do pedido protegem disputas de status e avaliações.
- Produção usa conta de banco restrita e migração separada; base nova operacional não recebe contas demonstrativas.

Não foram acrescentados pagamentos, chat, recuperação de senha ou verificação documental de identidade: os planos não os incluíam nesta implantação.

## Evidências locais

Ambiente: Windows, Java **24.0.1 compilando com release 17**, MySQL **8.0.46**, Maven Wrapper, Node 24/pnpm 11.25.0. Workflows usam Java 17; sua execução remota ainda depende da publicação. Bases exclusivas: `precisei_quality_test_20261106`, `precisei_production_test_20261106` e destino novo `precisei_20261106_restore_test`. A base pessoal `precisei` não foi migrada por esta atividade.

| Verificação | Resultado / evidência |
|---|---|
| `mvnw -Pstatic verify` | **101 testes Java aprovados**, zero falhas/erros/ignorados; `target/surefire-reports`, `artifacts/verify-final.log` |
| Análise estática SpotBugs | Bloqueio de alta prioridade aprovado; `target/spotbugsXml.xml` |
| JaCoCo | 573/734 linhas (**78,1%**); 174/324 decisões (**53,7%**); `target/site/jacoco/index.html`. Cobertura é diagnóstico, não certificação |
| Restrições SQL reais | **10/10 aprovadas**: UNIQUE, FK nula/inexistente, preço, status, data, notas e remoção referenciada; `artifacts/database-constraints.json` |
| Procedimentos de entrega | **10/10 testes Python aprovados**, incluindo origem, checksum, digest, compatibilidade e igualdade com homologação |
| Dependências e segredos | SBOM CycloneDX + Trivy 0.76.2: **zero achados altos/críticos** no inventário analisado; padrões de segredos sem achados no escopo varrido; `artifacts/trivy-components.json`, `artifacts/trivy-secrets.json`; `pnpm audit` aprovado |
| Correção de componentes | Spring Boot **4.1.1 → 4.1.2**, com Tomcat 11.0.27, Jackson 3.1.7 e Spring Security 7.1.2. A primeira varredura encontrou nove ocorrências altas/críticas no inventário anterior; não se declarou aprovação antes da correção |
| Navegador | **8 cenários aprovados**: Chromium 1366×768, 360×800 e 768×1024, mais Edge 1366×768; catálogo/formulários, axe, teclado, rotação de sessão e jornada até avaliação; `test-results/e2e.xml`, `artifacts/e2e.log` |
| Firefox | Dois cenários **bloqueados antes da execução**: runtime Windows não inicia por configuração lado a lado incorreta. Configuração existe na suíte/CI Linux; não foram contados como aprovados |
| Inspeção visual | Login desktop e página inicial móvel conferidos; `artifacts/login-desktop.png`, `artifacts/home-mobile.png` |
| Mutação PIT | Piloto de `SolicitacaoServico`: 15 mutantes, 3 mortos, **12 sem cobertura**, zero sobreviventes entre os cobertos; força nos cobertos 100%, escore global **20%**. `target/pit-reports/index.html`. Não extrapolar o resultado para todo o domínio; ampliar cobertura dos métodos não exercitados |
| Recuperação local | Dump MySQL cifrado com age, checksum e restore em base nova: oito tabelas com contagens iguais, sete FKs, migrações 1/2/3, **9,83 s** no exercício; `artifacts/recovery/result.json`. Chave privada temporária destruída. Não comprova RPO/RTO operacional |
| Senhas | Argon2id encode+verify: 1.491,3 ms na primeira amostra; 444,4/513,7 ms nas seguintes. `artifacts/password-benchmark.txt`; medição local curta, sujeita a aquecimento e carga |
| Fumaça operacional | `scripts/smoke.sh` aprovado no pacote Java local; páginas públicas disponíveis e clientes/pedidos redirecionam visitante para login |
| Carga curta | Massa acrescentada: 100 profissionais/1.000 pedidos sintéticos. k6, cinco usuários/30 s: **348 requisições, zero erros, p95 319,24 ms**; `artifacts/k6-smoke.json`. Escopo: catálogo público, uma execução curta; não equivale às três repetições do piloto de 1/5/10 usuários |
| Automação | YAML, actions por SHA, `actionlint`, sintaxe Bash e `docker compose config --quiet` com parâmetros fictícios aprovados. Build/execução de imagem, Compose em execução, Trivy de imagem/ZAP e deploy remoto **não executados**: mecanismo Linux do Docker indisponível localmente |

As evidências de execução são arquivos locais ignorados pelo Git para evitar versionar binários, sessões e dumps. A CI foi configurada para publicar relatórios como artefatos de execução com retenção de 30 dias. As contagens acima não somam testes bloqueados como êxitos.

## Inspeção: D01–D08

| Achado | Alteração e situação |
|---|---|
| D01 — Controle de acesso | `SegurancaConfig`, `AcessoService`, provider e filtros; isolamento por participante e testes negativos executados |
| D02 — Documentação | README, procedimentos, ameaças, governança e esta matriz atualizados; PDFs anteriores preservados |
| D03 — Rastreabilidade/aceite | Casos abaixo e roteiro em `aceitacao-publico.md`; aceite da versão nova com moradores/profissionais ainda requer participação real |
| D04 — Implantação | Dockerfile/Compose, migração, backup, fumaça, observação e retorno implementados; validação em servidor pendente |
| D05 — Acessibilidade | Resumo de erros, rótulos/ARIA, navegação e skip link, axe e três tamanhos; NVDA/zoom manual ainda pendentes |
| D06 — Testes | Serviços reais, binding HTTP, segurança, banco, concorrência, cobertura e piloto de mutação; lacunas registradas, sem aprovação presumida |
| D07 — Isolamento | Perfil test com Testcontainers por padrão, alternativa MySQL exclusiva e guard que recusa nomes fora do padrão de teste |
| D08 — Concorrência | Reteste sincronizado de e-mail duplicado, aceitação e avaliação; uma gravação/transição válida e resposta sem erro interno |

## Plano de testes: T01–T13 e CT01–CT18

| Tipo | Aplicação e limites |
|---|---|
| T01 — Revisão estática | Diff, links locais, SQL histórico, YAML/actions, SpotBugs e checklist de PR |
| T02 — Unidade | JUnit/Mockito em domínio, serviços, acesso, senha e limitador |
| T03 — Integração | Spring/HTTP/JPA/MySQL reais, bootstrap operacional e dez restrições SQL; Testcontainers preparado, execução local feita com MySQL isolado |
| T04 — Sistema | Playwright executa cadastro → login → solicitação → conclusão → avaliação |
| T05 — Regressão/fumaça | Suíte Java/E2E, prontidão de banco e smoke de operação |
| T06 — Segurança | ID forjado, papel/propriedade, CSRF, saída HTML, sessão, cabeçalhos e componentes; ZAP passivo configurado, ainda não executado |
| T07 — Acessibilidade | axe nas páginas públicas e pedidos, teclado e mensagens; inspeção assistiva manual não substituída por ferramenta |
| T08 — Usabilidade/aceite | Roteiro de tarefas versionado; coleta humana da versão implementada pendente |
| T09 — Compatibilidade | Chromium/Edge e três viewports verificados; Firefox bloqueado por runtime; Android físico não disponível |
| T10 — Carga | Script k6, massa de 100 profissionais/1.000 pedidos e workflow manual com três repetições; execução curta local registrada separadamente do piloto completo |
| T11 — Concorrência | Duas requisições sincronizadas para cada chave/operação disputada; repetição extensa do piloto não substituída por um par |
| T12 — Recuperação | Exercício local cifrado/restore aprovado; scripts operacionais aguardam exercício no servidor |
| T13 — Mutação | Piloto executado e métodos sem cobertura identificados; escore global continua abaixo de uma cobertura ampla |

| Caso | Evidência implementada / extensão ainda necessária |
|---|---|
| CT01 — Categoria | Validações de entidade/serviço/controller e UNIQUE SQL; fronteiras extensas de nome devem continuar na regressão |
| CT02 — Cliente | Serviço, encoder e cadastro/login E2E; senha em hash, sem texto puro |
| CT03 — Limites | Política 7/8 caracteres e 72 bytes; limites de nome, telefone, e-mail, duplicidade e binding HTTP |
| CT04 — Serviço/prestador | Validação de preço, FK SQL e associação; cadastro profissional E2E |
| CT05 — Pedido | Serviço e jornada real, titular da sessão e estado inicial PENDENTE |
| CT06 — Incompatibilidade | Serviço indisponível e referências inválidas recusados; restrições FK executadas |
| CT07 — Data/textos | Bean Validation, limite 1.000/1.001, data passada e CHECK SQL; casos adicionais de calendário ficam na regressão |
| CT08 — Estados | Todos os **25 pares** de transições mais fluxo/cancelamento e disputa simultânea |
| CT09 — Avaliação | Regra de conclusão/unicidade, limites de nota/comentário, média e fluxo E2E |
| CT10 — Acesso | Anônimo, cliente alheio, papel inválido, ID de titular forjado e POST sem CSRF |
| CT11 — Entradas/saída | Texto com script escapado, CSP e erro seguro; scanner passivo depende do contêiner |
| CT12 — Acessibilidade | Auditoria automática/teclado aprovados nos cenários executados; NVDA e zoom manual pendentes |
| CT13 — Aceite | Roteiro pronto; aprovação do público da versão nova não foi inventada |
| CT14 — Navegadores | Oito cenários aprovados; dois Firefox bloqueados e dispositivo físico pendente |
| CT15 — Carga | Dataset e execução automatizada completos preparados; carga curta local não equivale a três repetições completas nem teste autenticado |
| CT16 — Disputas | E-mail, status e avaliação sincronizados aprovados; ampliar quantidade de pares no piloto periódico |
| CT17 — Recuperação | Contagens, FKs, Flyway e tempo local medidos; incidente operacional, offsite e RPO/RTO dependem do servidor |
| CT18 — Repetibilidade | Suíte reexecutada, migrações validadas, bootstrap sem demos separado e PIT executado; recriação via contêiner ainda não verificada neste Windows |

## Desenvolvimento seguro: P01–P18

| Prática | Implementação / condição externa |
|---|---|
| P01 — Ameaças | `docs/seguranca/modelo-de-ameacas.md`, fronteiras e critérios |
| P02 — Dados | Exposição mínima por papel, contato privado e aviso; finalidade/retenção institucional exigem definição do responsável |
| P03 — Identidade | Login por tipo, ID e conta existente; sem atribuição de papel pelo cliente |
| P04 — Autorização | URL, listagem e serviço conferem vínculo; testes de acesso indevido |
| P05 — Senhas | Argon2id, BCrypt legado validado, política e utilitário interativo; benchmark local |
| P06 — Sessão | Rotação no login, timeout 30 min, logout, três sessões, expiração após troca, HttpOnly/SameSite/Secure em prod |
| P07 — CSRF | Spring Security habilitado; tokens Thymeleaf em POST e teste de recusa |
| P08 — Entrada/domínio | DTOs, validação HTTP/serviço, transações, FKs/UNIQUE/CHECK e lock |
| P09 — Saída | `th:text`, nenhuma saída livre `th:utext`, CSP e teste com script |
| P10 — Transporte | Caddy TLS, proxy confiável delimitado, cabeçalhos e cookies; DNS/certificado reais não configurados |
| P11 — Segredos | Variáveis externas, exemplos vazios, ignore, varredura e SSH restrito; credenciais antigas eventualmente publicadas devem ser rotacionadas pelo titular |
| P12 — Banco | Contas distintas app/migração/backup em configuração e Flyway separado; privilégios reais precisam ser concedidos no servidor |
| P13 — Ambientes | dev/test/prod/migrate, guard de teste, produção sem demos/root e migrações históricas imutáveis |
| P14 — Componentes | Patch estável, SBOM, Trivy, pnpm audit, Dependabot e reanálise semanal |
| P15 — Contribuição | Branch própria, templates de PR/defeito e checklist; proteção/revisor remoto exigem configuração no GitHub |
| P16 — CI/entrega | Workflows com ações por SHA, token mínimo, imagem testada, digest, manifesto e homologação antes de produção |
| P17 — Operação | Correlação, auditoria sem senha/e-mail, Actuator privado, Prometheus/alertas, runbook; canal/coletor/plantão exigem operador |
| P18 — Recuperação | Scripts cifrados/checksum/offsite/restore em base nova e exercício local; cópia fisicamente independente e ensaio operacional ainda necessários |

## Integração e entrega: CI01–CI09 / CD01–CD08

| Item | Automação implementada |
|---|---|
| CI01 | YAML, links locais, checksum das V1/V2 originais, checklist de contribuição |
| CI02 | SpotBugs e varredura de segredos com saída protegida |
| CI03 | Wrapper/JDK17, testes e relatórios mesmo em falha |
| CI04 | MySQL efêmero, dois bancos, Flyway, integridade SQL e concorrência |
| CI05 | SBOM/componentes/imagem, bloqueio de severidade alta/crítica e Dependabot |
| CI06 | Imagem em execução, ZAP passivo, Playwright/axe e evidências |
| CI07 | Base resolvida por digest, usuário não root; promoção do arquivo da imagem testada, ID e checksum do JAR conferidos |
| CI08 | Manifesto SHA/digest/esquema/evidências, notas preliminares e artefatos por execução |
| CI09 | Componentes/mutação semanais, links externos diários, carga manual de três repetições; cron de backup/monitoramento no servidor |
| CD01 | Origem da CI push autorizada, workflow/repositório/SHA/digest; manifesto de homologação comparado em produção |
| CD02 | Espaço, origem da imagem, serialização flock e backup; certificados/ambiente sob responsabilidade operacional |
| CD03 | Job migrate sem web, conta separada, Flyway/checksum e intervalo de esquema |
| CD04 | Compose baixa digest e atualiza aplicação/proxy, sem recriar MySQL |
| CD05 | Saúde, fumaça pública/controle de acesso e evidência de homologação; E2E sintético completo é feito sobre o candidato na CI |
| CD06 | Produção exige IDs de CI/homologação bem-sucedidos, mesmo digest e aprovador obrigatório |
| CD07 | Observação mínima de 15 min em produção, prontidão/fumaça e registro de versão atual |
| CD08 | Retorno somente com esquema compatível e saúde confirmada; sem recuperação segura, para app; banco não restaurado automaticamente |

Workflows e operações externas estão **implementados e verificados quanto à estrutura**, mas ainda não executados remotamente. O Docker Desktop local não disponibilizou o engine Linux; não houve build/scan de imagem nem execução Compose/ZAP/deploy. Nenhum push ou publicação foi realizado nesta atividade.

## Continuidade que depende de pessoas/infraestrutura

1. Revisar/publicar os commits, configurar proteção de branches, ambientes/aprovadores e secrets no GitHub.
2. Disponibilizar Docker Linux funcional e executar a CI completa; resolver a dependência lado a lado do Firefox Windows ou usar o runner Linux preparado.
3. Criar servidor, domínio, contas MySQL restritas, chaves, armazenamento independente e canal de alertas; exercitar migração/deploy/retorno/restore em homologação antes de liberar produção.
4. Executar o piloto completo de carga e tratar seus resultados; ampliar métodos sem cobertura no piloto de mutação.
5. Realizar NVDA/zoom/Android físico e aceite com moradores/profissionais, definindo finalidade, retenção e responsabilidades reais.

Consulte [operação e recuperação](../operacao/implantacao.md), [governança](../seguranca/governanca.md), [ameaças](../seguranca/modelo-de-ameacas.md) e [roteiro do público](aceitacao-publico.md).
