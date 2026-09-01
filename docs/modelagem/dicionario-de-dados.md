# Dicionário de dados

Este dicionário descreve o modelo lógico implementado para o Módulo 3. Os tipos foram definidos para MySQL 8 e materializados na migração `V1__criar_esquema_inicial.sql`.

## Tabela `categorias`

| Coluna | Tipo | Nulo | Chave | Regra |
|---|---|---:|---|---|
| `id_categoria` | `BIGINT` | Não | PK | Gerado automaticamente. |
| `nome` | `VARCHAR(80)` | Não | UK | Nome único da categoria. |

Restrições:

- chave primária em `id_categoria`;
- `nome` obrigatório e exclusivo;
- remoção impedida quando existirem serviços relacionados.

Situação: a migração V1 renomeia de forma controlada a coluna física anterior `id` para `id_categoria`, preservando os registros existentes. O atributo Java continua se chamando `id` e declara explicitamente seu vínculo com `id_categoria`.

## Tabela `servicos`

| Coluna | Tipo | Nulo | Chave | Regra |
|---|---|---:|---|---|
| `id_servico` | `BIGINT` | Não | PK | Gerado automaticamente. |
| `nome` | `VARCHAR(100)` | Não |  | Nome do serviço. |
| `descricao` | `VARCHAR(500)` | Não |  | Explicação objetiva da atividade. |
| `preco_referencia` | `DECIMAL(10,2)` | Sim |  | Quando informado, deve ser maior ou igual a zero. |
| `imagem` | `VARCHAR(255)` | Sim |  | Caminho ou URL da imagem. |
| `id_categoria` | `BIGINT` | Não | FK | Referencia `categorias.id_categoria`. |

Restrições:

- chave estrangeira obrigatória para `categorias`;
- preço não negativo;
- combinação de `nome` e `id_categoria` exclusiva;
- remoção impedida quando houver solicitações relacionadas;
- remoção das associações em `profissionais_servicos` quando o serviço for removido sem solicitações.

## Tabela `profissionais`

| Coluna | Tipo | Nulo | Chave | Regra |
|---|---|---:|---|---|
| `id_profissional` | `BIGINT` | Não | PK | Gerado automaticamente. |
| `nome` | `VARCHAR(120)` | Não |  | Nome do profissional. |
| `telefone` | `VARCHAR(20)` | Não |  | Telefone de contato. |
| `email` | `VARCHAR(160)` | Não | UK | E-mail exclusivo. |
| `senha_hash` | `VARCHAR(255)` | Não |  | Hash da senha; nunca texto puro. |
| `descricao` | `VARCHAR(1000)` | Sim |  | Apresentação profissional. |
| `cidade` | `VARCHAR(100)` | Não |  | Cidade de atuação. |
| `bairro` | `VARCHAR(100)` | Não |  | Bairro principal de atuação. |
| `disponivel` | `BOOLEAN` | Não |  | Padrão `TRUE`. |
| `foto_perfil` | `VARCHAR(255)` | Sim |  | Caminho ou URL da fotografia. |

Restrições:

- chave primária em `id_profissional`;
- e-mail obrigatório e exclusivo;
- nome, telefone, cidade, bairro e disponibilidade obrigatórios;
- remoção impedida quando existirem solicitações;
- associações com serviços removidas quando um profissional sem solicitações for excluído.

## Tabela `profissionais_servicos`

| Coluna | Tipo | Nulo | Chave | Regra |
|---|---|---:|---|---|
| `id_profissional` | `BIGINT` | Não | PK/FK | Referencia `profissionais.id_profissional`. |
| `id_servico` | `BIGINT` | Não | PK/FK | Referencia `servicos.id_servico`. |

Restrições:

- chave primária composta por `id_profissional` e `id_servico`;
- exclusão em cascata da associação quando um dos registros principais puder ser removido;
- duplicidade da mesma associação impedida pela chave composta.

## Tabela `clientes`

| Coluna | Tipo | Nulo | Chave | Regra |
|---|---|---:|---|---|
| `id_cliente` | `BIGINT` | Não | PK | Gerado automaticamente. |
| `nome` | `VARCHAR(120)` | Não |  | Nome do cliente. |
| `telefone` | `VARCHAR(20)` | Não |  | Telefone de contato. |
| `email` | `VARCHAR(160)` | Não | UK | E-mail exclusivo. |
| `senha_hash` | `VARCHAR(255)` | Não |  | Hash da senha. |

Restrições:

- chave primária em `id_cliente`;
- nome, telefone, e-mail e hash obrigatórios;
- e-mail exclusivo;
- remoção impedida quando existirem solicitações.

## Tabela `solicitacoes_servico`

| Coluna | Tipo | Nulo | Chave | Regra |
|---|---|---:|---|---|
| `id_solicitacao` | `BIGINT` | Não | PK | Gerado automaticamente. |
| `data_solicitacao` | `DATETIME` | Não |  | Data e hora de criação. |
| `data_agendada` | `DATETIME` | Sim |  | Não pode ser anterior à solicitação. |
| `status` | `VARCHAR(20)` | Não |  | Padrão `PENDENTE`; limitado aos estados previstos. |
| `descricao` | `VARCHAR(1000)` | Não |  | Descrição da necessidade do cliente. |
| `observacoes` | `VARCHAR(1000)` | Sim |  | Informações adicionais. |
| `id_cliente` | `BIGINT` | Não | FK | Referencia `clientes.id_cliente`. |
| `id_profissional` | `BIGINT` | Não | FK | Referencia `profissionais.id_profissional`. |
| `id_servico` | `BIGINT` | Não | FK | Referencia `servicos.id_servico`. |

Restrições:

- cliente, profissional e serviço obrigatórios;
- status limitado a `PENDENTE`, `ACEITA`, `EM_ANDAMENTO`, `CONCLUIDA` e `CANCELADA`;
- data agendada igual ou posterior à data da solicitação;
- exclusão dos registros relacionados impedida enquanto a solicitação existir;
- a camada de serviço deverá confirmar que o profissional oferece o serviço escolhido.

## Tabela `avaliacoes`

| Coluna | Tipo | Nulo | Chave | Regra |
|---|---|---:|---|---|
| `id_avaliacao` | `BIGINT` | Não | PK | Gerado automaticamente. |
| `nota` | `TINYINT` | Não |  | Valor inteiro de 1 a 5. |
| `comentario` | `VARCHAR(1000)` | Sim |  | Comentário do cliente. |
| `data_avaliacao` | `DATETIME` | Não |  | Data e hora do registro. |
| `id_solicitacao` | `BIGINT` | Não | FK/UK | Referencia exclusiva a `solicitacoes_servico`. |

Restrições:

- nota entre 1 e 5;
- uma avaliação por solicitação;
- solicitação obrigatória;
- a camada de serviço deverá permitir avaliação somente quando o status for `CONCLUIDA`;
- remoção da avaliação em cascata caso uma solicitação possa ser removida.

## Índices planejados

Além das chaves primárias e restrições de unicidade, serão criados índices para:

- serviço por categoria;
- profissional por cidade, bairro e disponibilidade;
- solicitação por cliente;
- solicitação por profissional;
- solicitação por status;
- solicitação por data;
- avaliação por solicitação.
