# Auditoria de requisitos do Módulo 3

Esta auditoria relaciona os requisitos da atividade às evidências existentes no projeto. A última verificação técnica foi realizada em 1º de setembro de 2026 com MySQL 8.0.46.

## Modelagem e manipulação do banco de dados

| Requisito | Situação | Evidência |
|---|---|---|
| Definir entidades, relacionamentos e restrições | Atendido | DER, dicionário de dados e regras de integridade em `docs/modelagem` |
| Implementar o esquema em SQL | Atendido | Migrações Flyway V1 e V2 em `src/main/resources/db/migration` |
| Executar inserções | Atendido | Migração V2 e inserções temporárias do roteiro de remoção |
| Executar consultas | Atendido | `docs/sql/01_consultas.sql` |
| Executar atualizações | Atendido | `docs/sql/02_atualizacoes.sql` |
| Executar remoções | Atendido | `docs/sql/03_remocoes.sql` |
| Comprovar restrições | Atendido | `docs/sql/04_testes_integridade.sql` e códigos MySQL registrados nas evidências |

As atualizações e remoções demonstrativas utilizam transações com `ROLLBACK`. Dessa forma, as operações são comprovadas sem danificar os dados acadêmicos. Os cinco testes negativos retornaram os códigos esperados `1062`, `3819` e `1451`.

## Integração da aplicação com o modelo

| Item | Situação |
|---|---|
| Entidades JPA correspondentes ao DER | Atendido |
| Relacionamento muitos para muitos entre profissionais e serviços | Atendido |
| Cadastro e edição de serviços e profissionais | Atendido |
| Cadastro de clientes | Atendido |
| Criação e acompanhamento de solicitações | Atendido |
| Transições controladas de status | Atendido |
| Avaliação exclusiva de solicitação concluída | Atendido |
| Média de avaliação calculada por profissional | Atendido |

## Controle de versão

| Requisito | Situação | Evidência |
|---|---|---|
| Utilizar Git | Atendido | Repositório com branches `main` e `modulo-3` |
| Organizar o projeto no GitHub | Atendido | Branch `modulo-3` publicada em `ermandotimoteo/precisei` |
| Registrar commits frequentes e explicativos | Atendido | Commits separados para modelagem, esquema, CRUD, integração JPA e fluxos da aplicação |

Histórico principal do módulo:

- `e4a6989` — modelagem de dados;
- `0e9b8a0` — esquema inicial com Flyway;
- `1f02d23` — operações CRUD;
- `27739fd` — integração JPA;
- `bfed87c` — serviços e profissionais;
- `cf20ef5` — clientes e solicitações;
- `cfd6870` — avaliações.

## Testes e conclusão

A suíte automatizada possui 37 testes aprovados, incluindo regras de negócio, controllers, inicialização do contexto, integração com MySQL e renderização das páginas principais. As telas também foram verificadas no navegador.

Todos os requisitos técnicos informados para o Módulo 3 estão atendidos. Como fechamento da entrega, resta apenas produzir o relatório acadêmico no modelo solicitado, caso ele seja exigido nesta etapa, e decidir quando integrar a branch `modulo-3` à branch principal.
