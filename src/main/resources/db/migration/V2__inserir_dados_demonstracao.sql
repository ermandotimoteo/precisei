-- Dados coerentes para demonstrar relacionamentos e consultas do Módulo 3.

INSERT IGNORE INTO categorias (nome) VALUES
    ('Chaveiro'),
    ('Elétrica'),
    ('Faxina'),
    ('Hidráulica'),
    ('Montagem de móveis'),
    ('Pintura');

INSERT INTO servicos (nome, descricao, preco_referencia, id_categoria) VALUES
    ('Instalação de tomada', 'Instalação ou substituição de tomada residencial.', 90.00,
        (SELECT id_categoria FROM categorias WHERE nome = 'Elétrica')),
    ('Reparo de vazamento', 'Correção de vazamento simples em pia ou torneira.', 120.00,
        (SELECT id_categoria FROM categorias WHERE nome = 'Hidráulica')),
    ('Faxina residencial', 'Limpeza geral de residência de pequeno porte.', 180.00,
        (SELECT id_categoria FROM categorias WHERE nome = 'Faxina')),
    ('Montagem de guarda-roupa', 'Montagem de guarda-roupa residencial.', 250.00,
        (SELECT id_categoria FROM categorias WHERE nome = 'Montagem de móveis'));

INSERT INTO profissionais (
    nome, telefone, email, senha_hash, descricao, cidade, bairro, disponivel
) VALUES
    ('Carlos Oliveira', '(83) 98888-1001', 'carlos.demo@precisei.local',
        '$2a$10$demonstracaoCarlosPreciseiModulo3',
        'Eletricista residencial com experiência em pequenos reparos.',
        'João Pessoa', 'Bancários', TRUE),
    ('Marcos Santos', '(83) 98888-1002', 'marcos.demo@precisei.local',
        '$2a$10$demonstracaoMarcosPreciseiModulo3',
        'Profissional de hidráulica e montagem de móveis.',
        'João Pessoa', 'Mangabeira', TRUE),
    ('Ana Lima', '(83) 98888-1003', 'ana.demo@precisei.local',
        '$2a$10$demonstracaoAnaPreciseiModulo3',
        'Profissional de limpeza residencial.',
        'João Pessoa', 'Tambaú', FALSE);

INSERT INTO clientes (nome, telefone, email, senha_hash) VALUES
    ('Maria Souza', '(83) 97777-2001', 'maria.demo@precisei.local',
        '$2a$10$demonstracaoMariaPreciseiModulo3'),
    ('João Pereira', '(83) 97777-2002', 'joao.demo@precisei.local',
        '$2a$10$demonstracaoJoaoPreciseiModulo3');

INSERT INTO profissionais_servicos (id_profissional, id_servico) VALUES
    ((SELECT id_profissional FROM profissionais WHERE email = 'carlos.demo@precisei.local'),
     (SELECT id_servico FROM servicos WHERE nome = 'Instalação de tomada')),
    ((SELECT id_profissional FROM profissionais WHERE email = 'marcos.demo@precisei.local'),
     (SELECT id_servico FROM servicos WHERE nome = 'Reparo de vazamento')),
    ((SELECT id_profissional FROM profissionais WHERE email = 'marcos.demo@precisei.local'),
     (SELECT id_servico FROM servicos WHERE nome = 'Montagem de guarda-roupa')),
    ((SELECT id_profissional FROM profissionais WHERE email = 'ana.demo@precisei.local'),
     (SELECT id_servico FROM servicos WHERE nome = 'Faxina residencial'));

INSERT INTO solicitacoes_servico (
    data_solicitacao, data_agendada, status, descricao, observacoes,
    id_cliente, id_profissional, id_servico
) VALUES
    ('2026-08-20 09:00:00', '2026-08-22 14:00:00', 'CONCLUIDA',
     'Trocar uma tomada danificada na sala.', 'Levar tomada de 20 amperes.',
     (SELECT id_cliente FROM clientes WHERE email = 'maria.demo@precisei.local'),
     (SELECT id_profissional FROM profissionais WHERE email = 'carlos.demo@precisei.local'),
     (SELECT id_servico FROM servicos WHERE nome = 'Instalação de tomada')),
    ('2026-08-25 10:30:00', '2026-09-03 09:00:00', 'ACEITA',
     'Reparar vazamento sob a pia da cozinha.', NULL,
     (SELECT id_cliente FROM clientes WHERE email = 'joao.demo@precisei.local'),
     (SELECT id_profissional FROM profissionais WHERE email = 'marcos.demo@precisei.local'),
     (SELECT id_servico FROM servicos WHERE nome = 'Reparo de vazamento')),
    ('2026-08-28 15:00:00', NULL, 'PENDENTE',
     'Solicitar orçamento para faxina do apartamento.', 'Apartamento com dois quartos.',
     (SELECT id_cliente FROM clientes WHERE email = 'maria.demo@precisei.local'),
     (SELECT id_profissional FROM profissionais WHERE email = 'ana.demo@precisei.local'),
     (SELECT id_servico FROM servicos WHERE nome = 'Faxina residencial'));

INSERT INTO avaliacoes (nota, comentario, data_avaliacao, id_solicitacao)
SELECT 5, 'Serviço realizado com cuidado e pontualidade.', '2026-08-22 16:30:00',
       id_solicitacao
FROM solicitacoes_servico
WHERE descricao = 'Trocar uma tomada danificada na sala.';
