# Precisei

Aplicação web de serviços domésticos do Projeto Integrador de Tecnologia da Informação (UFMS Digital). A evolução de Projeto Integrador III transforma os planos de inspeção, testes, desenvolvimento seguro e CI/CD em código e automações verificáveis.

Java 17, Spring Boot 4.1.2, MVC/Thymeleaf, JPA, MySQL 8, Flyway, Spring Security e Actuator. As páginas são renderizadas no servidor: GitHub Pages não executa esta aplicação Java.

## Funcionalidades e acesso

- Catálogo público, categorias (incluindo Faxina) e apresentação profissional.
- Login por tipo de conta, sessão, logout, limite de tentativas e CSRF.
- Cliente cria pedidos em seu nome, consulta seus atendimentos, cancela quando permitido e avalia conclusão própria.
- Profissional edita seu perfil e aceita/inicia/conclui pedidos atribuídos a ele.
- Manutenção gerencia catálogo; conta externa, sem senha padrão/autocadastro de privilégios.
- Dados de clientes e contatos do atendimento restritos aos envolvidos/manutenção.
- Formulários com validações, resumo de erros, campos associados a mensagens e layout responsivo.
- Transações, restrições SQL e lock do pedido para avaliações/status concorrentes.
- Novas senhas com Argon2id. BCrypt válido legado é verificado e atualizado gradualmente no login, sem converter senha não verificada.

Preços são referências, não orçamento confirmado. Pagamentos, chat, recuperação de senha e confirmação de mudança de e-mail não integram esta entrega.

## Executar localmente no Windows

Pré-requisitos: JDK 17 ou superior compatível, `JAVA_HOME` configurado e MySQL 8 iniciado. Maven Wrapper incluído.

Crie um banco de desenvolvimento vazio ou preserve o banco `precisei` existente. V1/V2 históricas não foram alteradas. No PowerShell da pasta:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/precisei'
$env:DB_USERNAME = 'SEU_USUARIO_LOCAL'
$env:DB_PASSWORD = 'SUA_SENHA_LOCAL'
.\mvnw.cmd spring-boot:run
```

Acesse `http://localhost:8080`. Cliente se cadastra em `/cadastro/cliente`; profissional em `/profissionais`. Entre em `/login` selecionando o tipo de conta. Mantenha o processo aberto durante o uso; `Ctrl+C` encerra o servidor.

Arquivos `.env` **não são carregados automaticamente pelo Spring**. São referência e configuração para Compose; não versione cópias preenchidas.

### Manutenção

Gere hash em terminal interativo com `.\scripts\hash-password.ps1`; defina `ADMIN_EMAIL` e `ADMIN_PASSWORD_HASH` como variáveis protegidas e reinicie a aplicação. Não existe senha administrativa embutida. O utilitário não aceita a senha por argumento.

Registros `.demo@precisei.local` da V2 são fixtures históricas, **não credenciais utilizáveis**. Cadastre sua própria conta. Produção recusa essas contas demonstrativas sem apagá-las automaticamente.

## Testes

```powershell
.\mvnw.cmd verify
```

Os testes Spring ativam `test` e, por padrão, usam MySQL descartável via Testcontainers (exige Docker Linux disponível). Sem Docker, crie **dois bancos exclusivos vazios**, contendo `_test` no nome, e configure:

```powershell
$env:TEST_DB_URL = 'jdbc:mysql://localhost:3306/precisei_test'
$env:TEST_PROD_DB_URL = 'jdbc:mysql://localhost:3306/precisei_production_test'
$env:TEST_DB_USERNAME = 'USUARIO_DE_TESTE'
$env:TEST_DB_PASSWORD = 'SENHA_DE_TESTE'
$env:TEST_DB_DRIVER = 'com.mysql.cj.jdbc.Driver'
.\mvnw.cmd verify
```

A conta pode criar/alterar tabelas nesses bancos, nunca na base real. Bootstrap operacional é testado separadamente sem contas/pedidos de demonstração. Relatórios: `target/surefire-reports`, `target/site/jacoco/index.html` e SBOM em `target/classes/META-INF/sbom/`.

### Navegador e acessibilidade

Node.js 24 e pnpm 11.25.0. Inicie a aplicação em base **sintética de teste** com catálogo disponível:

```powershell
pnpm install --frozen-lockfile
pnpm exec playwright install chromium firefox
pnpm check
$env:BASE_URL = 'http://127.0.0.1:8080'
pnpm test:e2e
```

O E2E cria contas/pedidos sintéticos: não apontar para produção. Chromium em três tamanhos, Firefox e axe-core complementam leitor de tela e aceite humano. Para incluir Edge instalado no Windows, defina `$env:EDGE_TEST = '1'`. Evidências em `playwright-report/` e `test-results/` são ignoradas no Git.

### Operação, mutação e carga

```powershell
python -m unittest discover -s tests/ops -v
.\mvnw.cmd -Pmutation test-compile org.pitest:pitest-maven:mutationCoverage
.\scripts\hash-password.ps1 --benchmark
```

`tests/load/catalogo.js` contém o piloto k6 com 1/5/10 usuários. Limiares p95 < 2s e erros < 1% são provisórios; preparar dataset e medir sem outras cargas concorrentes. Os 37 testes dos relatórios antigos são históricos; consultar [evidências atuais](docs/qualidade/implementacao.md).

`tests/database/constraints.py --database precisei_test` retesta dez restrições no MySQL com fixtures e rollback; use `MYSQL_PWD`/`MYSQL_USER`/`MYSQL_HOST` externos e opcional `--mysql` para o caminho do executável. Recusa a base operacional.
`scripts/seed-load.py --database precisei_load_test` prepara 100 profissionais/1.000 pedidos em uma base **já migrada e exclusiva**; contas sintéticas não permitem login. O workflow manual **Piloto de carga** prepara seu próprio banco e executa três repetições completas. `LOAD_SMOKE=1` seleciona somente a carga curta de cinco usuários/30 s.

`scripts/exercise-recovery.py` permite ensaio local cifrado com MySQL/age, exige origem de teste e destino novo `_restore_test`, compara contagens e não sobrescreve bases. Informe `--source`/`--destination` e, se necessário, caminhos das ferramentas com `--mysql`, `--mysqldump`, `--age` e `--age-keygen`. O exercício não substitui o restore operacional em homologação.

## Migrações e produção

- Base histórica: `db/migration` (V1/V2 originais) + `db/common` (V3+).
- Base operacional **nova**: `db/production` (V1 esquema e V2 categorias sem contas/pedidos) + `db/common`.
- Nunca trocar a linha de origem de um banco já migrado. Futuras mudanças são comuns em V3+; não executar `repair`/`baseline` para ocultar incompatibilidade.
- V3 cria índices de consultas por participante/data. Hibernate apenas valida o esquema.
- Perfil `prod` não executa Flyway; job `migrate` usa conta de DDL separada, sem servidor web. Conta da aplicação não tem DDL/administração.

Veja [implantação e recuperação](docs/operacao/implantacao.md).

## CI/CD e limites de ativação

`CI`: validação, MySQL/testes, cobertura/SBOM, imagem, scanners e E2E. Publicação em push confiável promove a **imagem testada** sem reconstruí-la.
`CD`: valida procedência, manifesto e digest; homologação automática e produção por disparo/aprovação protegida. Produção recusa ambiente sem aprovador obrigatório. Actions por SHA, token mínimo e SSH de comando restrito.
Qualidade periódica inclui mutação; Dependabot propõe atualizações. Backup criptografado, restore em base nova e monitoramento possuem scripts próprios.

Workflows só serão ativados no GitHub quando publicados. Segredos, revisores, servidor, DNS/TLS, registro e notificações exigem configuração real pelo responsável. Esta implementação não publica/implanta automaticamente o projeto.

## Documentação

- [Matriz de implementação e verificações](docs/qualidade/implementacao.md)
- [Modelo de ameaças](docs/seguranca/modelo-de-ameacas.md)
- [Governança, incidentes e IA](docs/seguranca/governanca.md)
- [Aceite com o público](docs/qualidade/aceitacao-publico.md)
- [Operação e implantação](docs/operacao/implantacao.md)
- [DER](docs/modelagem/der.md), [dicionário](docs/modelagem/dicionario-de-dados.md) e [regras](docs/modelagem/regras-de-integridade.md)

Os PDFs de `output/pdf/` preservam o planejamento acadêmico de suas datas. A matriz distingue implementação, comprovação e dependências humanas/operacionais.

Autor: **Ermando Timotio de Sousa Filho**. Referência: moradores e profissionais autônomos de João Pessoa - PB.
