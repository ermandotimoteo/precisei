# Precisei

O **Precisei** é uma aplicação web acadêmica criada para organizar categorias de pequenos serviços domésticos e apoiar, em etapas futuras, a conexão entre moradores e profissionais autônomos.

O projeto faz parte do **Projeto Integrador de Tecnologia da Informação II**. O Módulo 2 entregou uma solução web dinâmica, responsiva e integrada ao MySQL. No Módulo 3, o projeto evolui a modelagem, a manipulação do banco de dados e o histórico de versionamento no Git.

## Objetivo

Desenvolver uma aplicação web responsiva utilizando um framework moderno, HTML semântico, CSS e integração com banco de dados, mantendo uma estrutura simples e adequada ao contexto acadêmico.

O público-alvo conceitual é formado por:

- moradores que necessitam de pequenos serviços ou reparos domésticos;
- profissionais autônomos que desejam divulgar suas áreas de atuação.

## Tecnologias utilizadas

- Java 17;
- Spring Boot 4.1.1;
- Spring Web MVC;
- Thymeleaf;
- Spring Data JPA;
- Hibernate;
- Flyway para versionamento do esquema SQL;
- Spring Validation;
- MySQL 8;
- HTML5;
- CSS3;
- Maven e Maven Wrapper;
- JUnit, Mockito e MockMvc para testes.

## Funcionalidades implementadas

- página inicial responsiva;
- navegação entre Início, Serviços, Profissionais, Clientes e Solicitações;
- listagem dinâmica de categorias armazenadas no MySQL;
- cadastro de categorias de serviço;
- validação de nome obrigatório e tamanho máximo;
- prevenção de categorias duplicadas;
- mensagens de validação e confirmação no formulário;
- carga inicial de categorias sem gerar duplicações;
- ícones SVG específicos para as categorias;
- página de profissionais integrada aos perfis reais do banco;
- catálogo de serviços carregado do MySQL;
- listagem de profissionais, localização, disponibilidade e serviços oferecidos;
- mapeamento JPA de todas as entidades e relacionamentos do DER;
- cadastro e edição de serviços com categoria e preço de referência;
- cadastro e edição de profissionais com associação a vários serviços;
- armazenamento de novas senhas profissionais com hash BCrypt;
- cadastro e listagem de clientes, com validação de e-mail único e senha protegida por BCrypt;
- criação de solicitações com cliente, profissional, serviço, descrição e agendamento opcional;
- validação da disponibilidade do profissional e dos serviços que ele oferece;
- listagem e acompanhamento das solicitações;
- atualização controlada do status entre Pendente, Aceita, Em andamento, Concluída e Cancelada;
- avaliação de solicitações concluídas com nota de 1 a 5 e comentário opcional;
- bloqueio de avaliações duplicadas e cálculo da média de cada profissional;
- testes unitários e de integração.

Categorias iniciais:

- Chaveiro;
- Elétrica;
- Faxina;
- Hidráulica;
- Montagem de móveis;
- Pintura.

## Escopo atual

Esta versão representa uma **solução inicial dinâmica para o Módulo 2**, e não o MVP completo da plataforma.

Ainda não foram implementados pela interface:

- autenticação de usuários;
- pesquisa de profissionais por localização e disponibilidade;
- recuperação de senha e gerenciamento de sessão.

Essas funcionalidades estão previstas para etapas futuras do projeto.

## Arquitetura

A aplicação segue uma arquitetura em camadas:

```text
Navegador
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
JPA / Hibernate
    ↓
MySQL
```

As páginas são renderizadas pelo Thymeleaf:

```text
Controller → Model → Thymeleaf → HTML → CSS
```

## Modelagem de dados — Módulo 3

A evolução planejada do banco está documentada em:

- [Modelo de dados e DER](docs/modelagem/der.md);
- [Dicionário de dados](docs/modelagem/dicionario-de-dados.md);
- [Regras de integridade e negócio](docs/modelagem/regras-de-integridade.md).

O modelo inclui Categoria, Serviço, Profissional, Cliente, Solicitação de Serviço e Avaliação, além da associação muitos para muitos entre profissionais e serviços. O esquema correspondente está implementado em migrações Flyway e todas as tabelas estão mapeadas em entidades JPA.

### Esquema SQL versionado

A migração inicial está em [`V1__criar_esquema_inicial.sql`](src/main/resources/db/migration/V1__criar_esquema_inicial.sql). Ela:

- preserva as categorias cadastradas no Módulo 2;
- padroniza a chave primária como `id_categoria`;
- cria as tabelas, chaves estrangeiras e restrições do DER;
- cria índices para as consultas planejadas;
- é registrada automaticamente na tabela `flyway_schema_history`.

O Hibernate está configurado com `ddl-auto=validate`: ele verifica o mapeamento das entidades, mas não altera o banco silenciosamente. As mudanças estruturais passam a ser responsabilidade das migrações SQL versionadas.

A migração [`V2__inserir_dados_demonstracao.sql`](src/main/resources/db/migration/V2__inserir_dados_demonstracao.sql) fornece dados relacionados para testar o modelo. As operações exigidas no Módulo 3 estão disponíveis em:

- [`01_consultas.sql`](docs/sql/01_consultas.sql): consultas simples, relacionais, agregações e filtros;
- [`02_atualizacoes.sql`](docs/sql/02_atualizacoes.sql): atualizações demonstradas dentro de uma transação reversível;
- [`03_remocoes.sql`](docs/sql/03_remocoes.sql): remoções seguras de registros temporários;
- [`04_testes_integridade.sql`](docs/sql/04_testes_integridade.sql): tentativas inválidas para comprovar restrições;
- [evidências da execução](docs/banco/evidencias-crud.md): resultados observados no MySQL.

## Estrutura principal

```text
src/
├── main/
│   ├── java/com/precisei/
│   │   ├── config/       # Carga dos dados iniciais
│   │   ├── controller/   # Rotas e requisições HTTP
│   │   ├── model/        # Entidades persistentes
│   │   ├── repository/   # Acesso ao banco de dados
│   │   └── service/      # Regras de negócio
│   └── resources/
│       ├── static/css/   # Estilização responsiva
│       ├── templates/    # Páginas e fragmentos Thymeleaf
│       ├── db/migration/ # Migrações SQL executadas pelo Flyway
│       └── application.properties
└── test/java/com/precisei/ # Testes unitários e de integração
```

## Pré-requisitos

Antes de executar o projeto, instale:

- Java Development Kit (JDK) 17 ou superior compatível;
- MySQL Community Server 8;
- Git, somente para clonagem e controle de versão.

Não é necessário instalar o Maven separadamente, pois o projeto contém o Maven Wrapper.

## Configuração do banco de dados

1. Inicie o servidor MySQL.

2. Crie apenas o banco de dados vazio:

```sql
CREATE DATABASE precisei
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

Não crie as tabelas manualmente. Ao iniciar a aplicação, o Flyway executará as migrações pendentes. Se você já utilizava o banco da versão anterior, a primeira migração preservará as categorias existentes e criará o restante do esquema.

3. Configure a senha do MySQL por variável de ambiente.

No PowerShell:

```powershell
$env:DB_PASSWORD = "SUA_SENHA_LOCAL"
```

Se o usuário do MySQL não for `root`, configure também:

```powershell
$env:DB_USERNAME = "SEU_USUARIO"
```

Se a URL do banco for diferente da configuração padrão, utilize:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/precisei"
```

As variáveis definidas dessa forma permanecem disponíveis somente na janela atual do PowerShell. Isso evita armazenar credenciais reais no código-fonte.

## Como executar no Windows

Abra o PowerShell na pasta do projeto, configure as variáveis necessárias e execute:

```powershell
.\mvnw.cmd spring-boot:run
```

Quando o terminal informar que a aplicação foi iniciada, acesse:

```text
http://localhost:8080
```

Mantenha o PowerShell aberto enquanto estiver utilizando a aplicação. Ao encerrar o processo ou fechar a janela, o servidor local será desligado.

Para interromper a execução, pressione `Ctrl + C`.

## Rotas disponíveis

| Método | Rota | Descrição |
|---|---|---|
| GET | `/` | Exibe a página inicial e as categorias |
| GET | `/servicos` | Lista categorias e apresenta o formulário |
| POST | `/servicos` | Valida e cadastra uma categoria |
| POST | `/servicos/novo` | Valida e cadastra um serviço |
| GET | `/servicos/{id}/editar` | Exibe o formulário de edição do serviço |
| POST | `/servicos/{id}/editar` | Atualiza um serviço |
| GET | `/profissionais` | Lista profissionais e apresenta o formulário de cadastro |
| POST | `/profissionais/novo` | Valida e cadastra um profissional |
| GET | `/profissionais/{id}/editar` | Exibe o formulário de edição do profissional |
| POST | `/profissionais/{id}/editar` | Atualiza um profissional |
| GET | `/clientes` | Lista clientes e apresenta o formulário de cadastro |
| POST | `/clientes/novo` | Valida e cadastra um cliente |
| GET | `/solicitacoes` | Lista solicitações e apresenta o formulário de criação |
| POST | `/solicitacoes/novo` | Valida e cria uma solicitação |
| POST | `/solicitacoes/{id}/status` | Atualiza o status conforme as transições permitidas |
| POST | `/solicitacoes/{id}/avaliacao` | Avalia uma solicitação concluída |

## Como executar os testes

Com o MySQL iniciado e a variável `DB_PASSWORD` configurada, execute:

```powershell
.\mvnw.cmd test
```

Na última validação foram executados **37 testes**, sem falhas ou erros.

## Responsividade e acessibilidade

A interface utiliza HTML semântico, incluindo `header`, `nav`, `main`, `section`, `article`, `form`, `label`, `button` e `footer`.

Também foram aplicados:

- idioma da página definido como português do Brasil;
- rótulos associados aos campos do formulário;
- indicação da página atual na navegação;
- mensagens de erro associadas ao campo inválido;
- foco visível para links, campos e botões;
- SVGs decorativos ocultos das tecnologias assistivas;
- Grid, Flexbox e media queries para adaptação da interface.

## Próximas etapas

- permitir filtros por categoria, localização e disponibilidade;
- adicionar autenticação em uma etapa posterior;
- criar recuperação de senha e gerenciamento de sessão.

## Contexto acadêmico

Projeto desenvolvido para o **Projeto Integrador de Tecnologia da Informação II**. O Módulo 2 abordou desenvolvimento web com frameworks e HTML/CSS; o Módulo 3 aborda modelagem, manipulação de banco de dados e controle de versão.

Local de referência do projeto: João Pessoa - PB, Brasil.

## Autor

**Nome:** Ermando Timotio de Sousa Filho.
