# Precisei

O **Precisei** é uma aplicação web acadêmica criada para organizar categorias de pequenos serviços domésticos e apoiar, em etapas futuras, a conexão entre moradores e profissionais autônomos.

O projeto faz parte do **Projeto Integrador de Tecnologia da Informação II** e, nesta entrega do Módulo 2, demonstra o desenvolvimento de uma solução web dinâmica, responsiva e integrada a um banco de dados relacional.

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
- Spring Validation;
- MySQL 8;
- HTML5;
- CSS3;
- Maven e Maven Wrapper;
- JUnit, Mockito e MockMvc para testes.

## Funcionalidades implementadas

- página inicial responsiva;
- navegação entre Início, Profissionais e Serviços;
- listagem dinâmica de categorias armazenadas no MySQL;
- cadastro de categorias de serviço;
- validação de nome obrigatório e tamanho máximo;
- prevenção de categorias duplicadas;
- mensagens de validação e confirmação no formulário;
- carga inicial de categorias sem gerar duplicações;
- ícones SVG específicos para as categorias;
- página introdutória de profissionais com categorias reais do banco;
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

Ainda não foram implementados:

- cadastro e edição de perfis de profissionais;
- cadastro e autenticação de usuários;
- pesquisa de profissionais por localização e disponibilidade;
- solicitação de atendimento;
- acompanhamento de status;
- avaliações de profissionais.

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

2. Crie o banco de dados:

```sql
CREATE DATABASE precisei
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

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
| GET | `/profissionais` | Exibe a solução inicial do módulo de profissionais |

## Como executar os testes

Com o MySQL iniciado e a variável `DB_PASSWORD` configurada, execute:

```powershell
.\mvnw.cmd test
```

Na última validação da entrega foram executados **14 testes**, sem falhas ou erros.

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

- implementar os perfis reais de profissionais;
- relacionar profissionais às categorias;
- permitir filtros por categoria, localização e disponibilidade;
- implementar solicitações e atualização de status;
- adicionar autenticação em uma etapa posterior;
- publicar o código no GitHub e inserir o endereço do repositório no relatório.

## Contexto acadêmico

Projeto desenvolvido para o **Projeto Integrador de Tecnologia da Informação II**, no Módulo 2 de Desenvolvimento Web com frameworks e HTML/CSS.

Local de referência do projeto: João Pessoa - PB, Brasil.

## Autor

**Nome:** Ermando Timotio de Sousa Filho.
