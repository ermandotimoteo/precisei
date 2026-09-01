-- Demonstra UPDATE sem modificar permanentemente os dados acadêmicos.
START TRANSACTION;

UPDATE profissionais
SET disponivel = FALSE,
    bairro = 'Jardim Cidade Universitária'
WHERE email = 'carlos.demo@precisei.local';

UPDATE servicos
SET preco_referencia = 135.00
WHERE nome = 'Reparo de vazamento'
  AND id_categoria = (
      SELECT id_categoria FROM categorias WHERE nome = 'Hidráulica'
  );

UPDATE solicitacoes_servico
SET status = 'EM_ANDAMENTO'
WHERE descricao = 'Reparar vazamento sob a pia da cozinha.'
  AND status = 'ACEITA';

SELECT nome, bairro, disponivel
FROM profissionais
WHERE email = 'carlos.demo@precisei.local';

SELECT nome, preco_referencia
FROM servicos
WHERE nome = 'Reparo de vazamento';

SELECT id_solicitacao, status
FROM solicitacoes_servico
WHERE descricao = 'Reparar vazamento sob a pia da cozinha.';

ROLLBACK;
