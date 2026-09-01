-- Execute cada comando separadamente.
-- Cada comando deve falhar e comprova uma restrição do esquema.

-- Falha esperada: e-mail de cliente duplicado.
INSERT INTO clientes (nome, telefone, email, senha_hash)
VALUES ('Duplicado', '(83) 90000-0001',
        'maria.demo@precisei.local', '$2a$10$testeDuplicidadeModulo3');

-- Falha esperada: preço de referência negativo.
INSERT INTO servicos (nome, descricao, preco_referencia, id_categoria)
SELECT 'Preço inválido', 'Teste da restrição de preço.', -1.00, id_categoria
FROM categorias WHERE nome = 'Elétrica';

-- Falha esperada: status fora do domínio permitido.
UPDATE solicitacoes_servico
SET status = 'DESCONHECIDA'
WHERE descricao = 'Solicitar orçamento para faxina do apartamento.';

-- Falha esperada: nota fora do intervalo de 1 a 5.
UPDATE avaliacoes SET nota = 6 LIMIT 1;

-- Falha esperada: categoria referenciada por serviços.
DELETE FROM categorias WHERE nome = 'Elétrica';
