# Evidências de manipulação do banco de dados

Os scripts desta etapa foram executados em 1º de setembro de 2026 no MySQL 8.0.46. A migração Flyway V2 foi aplicada com sucesso antes das operações e inseriu dados relacionados em todas as entidades do modelo.

## Dados inseridos

| Tabela | Registros de demonstração |
|---|---:|
| `categorias` | 6 |
| `servicos` | 4 |
| `profissionais` | 3 |
| `clientes` | 2 |
| `profissionais_servicos` | 4 |
| `solicitacoes_servico` | 3 |
| `avaliacoes` | 1 |

Os registros incluem três profissionais, dois clientes, quatro serviços, associações muitos para muitos, solicitações em estados diferentes e uma avaliação de uma solicitação concluída.

## Consultas executadas

O arquivo [`01_consultas.sql`](../sql/01_consultas.sql) demonstrou:

- serviços agrupados com suas categorias;
- profissionais e os serviços que oferecem;
- solicitações com cliente, profissional e serviço;
- total de solicitações por status;
- média de avaliações calculada por profissional;
- filtro de profissional disponível por cidade e serviço.

Resultados relevantes:

- foram retornados quatro serviços com suas categorias;
- as três solicitações retornaram os estados `CONCLUIDA`, `ACEITA` e `PENDENTE`;
- Carlos Oliveira obteve média `5,00` a partir de uma avaliação;
- a consulta por profissional disponível para reparo de vazamento retornou Marcos Santos.

## Atualizações executadas

O arquivo [`02_atualizacoes.sql`](../sql/02_atualizacoes.sql) foi executado dentro de uma transação. Durante a demonstração:

- a disponibilidade e o bairro de um profissional foram alterados;
- o preço de referência de um serviço passou temporariamente de `120,00` para `135,00`;
- uma solicitação passou temporariamente de `ACEITA` para `EM_ANDAMENTO`.

As consultas realizadas antes do `ROLLBACK` comprovaram os novos valores. A reversão restaurou os dados originais.

## Remoções executadas

O arquivo [`03_remocoes.sql`](../sql/03_remocoes.sql) criou e removeu:

- um cliente temporário sem solicitações;
- um serviço temporário sem relacionamentos.

As duas contagens posteriores ao `DELETE` retornaram zero. O roteiro também termina com `ROLLBACK`, garantindo que a demonstração seja repetível.

## Restrições comprovadas

Os comandos de [`04_testes_integridade.sql`](../sql/04_testes_integridade.sql) foram executados separadamente e produziram as falhas esperadas:

| Tentativa inválida | Restrição comprovada | Código MySQL |
|---|---|---:|
| Repetir o e-mail de um cliente | `uk_clientes_email` | 1062 |
| Inserir preço negativo | `ck_servicos_preco` | 3819 |
| Usar status inexistente | `ck_solicitacoes_status` | 3819 |
| Atribuir nota igual a 6 | `ck_avaliacoes_nota` | 3819 |
| Excluir categoria utilizada | `fk_servicos_categoria` | 1451 |

As falhas são o resultado correto: nenhuma operação inválida foi persistida.

## Como reproduzir

Com o banco configurado e após iniciar a aplicação ao menos uma vez para executar as migrações, abra o cliente MySQL na raiz do projeto e utilize:

```sql
SOURCE docs/sql/01_consultas.sql;
SOURCE docs/sql/02_atualizacoes.sql;
SOURCE docs/sql/03_remocoes.sql;
```

Os comandos do arquivo `04_testes_integridade.sql` devem ser executados individualmente, pois o resultado esperado de cada um é um erro de integridade.
