-- Consultas de demonstração. Este arquivo não altera dados.

-- 1. Serviços com suas categorias.
SELECT s.id_servico, s.nome AS servico, c.nome AS categoria,
       s.preco_referencia
FROM servicos s
JOIN categorias c ON c.id_categoria = s.id_categoria
ORDER BY c.nome, s.nome;

-- 2. Serviços oferecidos por cada profissional.
SELECT p.nome AS profissional, p.cidade, p.bairro, p.disponivel,
       s.nome AS servico, c.nome AS categoria
FROM profissionais p
JOIN profissionais_servicos ps
  ON ps.id_profissional = p.id_profissional
JOIN servicos s ON s.id_servico = ps.id_servico
JOIN categorias c ON c.id_categoria = s.id_categoria
ORDER BY p.nome, s.nome;

-- 3. Visão completa das solicitações.
SELECT ss.id_solicitacao, cl.nome AS cliente, p.nome AS profissional,
       s.nome AS servico, ss.status, ss.data_solicitacao, ss.data_agendada
FROM solicitacoes_servico ss
JOIN clientes cl ON cl.id_cliente = ss.id_cliente
JOIN profissionais p ON p.id_profissional = ss.id_profissional
JOIN servicos s ON s.id_servico = ss.id_servico
ORDER BY ss.data_solicitacao;

-- 4. Quantidade de solicitações por status.
SELECT status, COUNT(*) AS quantidade
FROM solicitacoes_servico
GROUP BY status
ORDER BY status;

-- 5. Média calculada das avaliações por profissional.
SELECT p.id_profissional, p.nome AS profissional,
       ROUND(AVG(a.nota), 2) AS avaliacao_media,
       COUNT(a.id_avaliacao) AS total_avaliacoes
FROM profissionais p
LEFT JOIN solicitacoes_servico ss
  ON ss.id_profissional = p.id_profissional
LEFT JOIN avaliacoes a ON a.id_solicitacao = ss.id_solicitacao
GROUP BY p.id_profissional, p.nome
ORDER BY avaliacao_media DESC, p.nome;

-- 6. Profissionais disponíveis para um serviço em João Pessoa.
SELECT DISTINCT p.nome, p.bairro, s.nome AS servico
FROM profissionais p
JOIN profissionais_servicos ps
  ON ps.id_profissional = p.id_profissional
JOIN servicos s ON s.id_servico = ps.id_servico
WHERE p.cidade = 'João Pessoa'
  AND p.disponivel = TRUE
  AND s.nome = 'Reparo de vazamento';
