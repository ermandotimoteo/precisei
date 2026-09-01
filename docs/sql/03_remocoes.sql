-- Demonstra DELETE somente com registros temporários e usa ROLLBACK.
START TRANSACTION;

INSERT INTO clientes (nome, telefone, email, senha_hash)
VALUES ('Cliente Temporário', '(83) 90000-0000',
        'temporario@precisei.local', '$2a$10$registroTemporarioModulo3');

DELETE FROM clientes
WHERE email = 'temporario@precisei.local'
  AND NOT EXISTS (
      SELECT 1 FROM solicitacoes_servico ss
      WHERE ss.id_cliente = clientes.id_cliente
  );

SELECT COUNT(*) AS cliente_temporario_restante
FROM clientes
WHERE email = 'temporario@precisei.local';

INSERT INTO servicos (nome, descricao, preco_referencia, id_categoria)
SELECT 'Serviço temporário', 'Registro criado somente para demonstrar DELETE.',
       10.00, id_categoria
FROM categorias
WHERE nome = 'Chaveiro';

DELETE FROM servicos
WHERE nome = 'Serviço temporário'
  AND NOT EXISTS (
      SELECT 1 FROM solicitacoes_servico ss
      WHERE ss.id_servico = servicos.id_servico
  );

SELECT COUNT(*) AS servico_temporario_restante
FROM servicos
WHERE nome = 'Serviço temporário';

ROLLBACK;
