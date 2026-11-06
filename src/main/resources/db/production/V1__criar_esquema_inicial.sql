-- Esquema inicial versionado do Precisei para MySQL 8.
-- A criação prévia de categorias torna a migração compatível tanto com um
-- banco novo quanto com a tabela criada pelo Hibernate no Módulo 2.

CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(80) NOT NULL,
    CONSTRAINT pk_categorias PRIMARY KEY (id),
    CONSTRAINT uk_categorias_nome UNIQUE (nome)
) ENGINE = InnoDB;

ALTER TABLE categorias
    RENAME COLUMN id TO id_categoria;

CREATE TABLE servicos (
    id_servico BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    preco_referencia DECIMAL(10, 2) NULL,
    imagem VARCHAR(255) NULL,
    id_categoria BIGINT NOT NULL,
    CONSTRAINT pk_servicos PRIMARY KEY (id_servico),
    CONSTRAINT uk_servicos_nome_categoria UNIQUE (nome, id_categoria),
    CONSTRAINT ck_servicos_preco CHECK (
        preco_referencia IS NULL OR preco_referencia >= 0
    ),
    CONSTRAINT fk_servicos_categoria FOREIGN KEY (id_categoria)
        REFERENCES categorias (id_categoria)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE profissionais (
    id_profissional BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(160) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    descricao VARCHAR(1000) NULL,
    cidade VARCHAR(100) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    foto_perfil VARCHAR(255) NULL,
    CONSTRAINT pk_profissionais PRIMARY KEY (id_profissional),
    CONSTRAINT uk_profissionais_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE clientes (
    id_cliente BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(160) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    CONSTRAINT pk_clientes PRIMARY KEY (id_cliente),
    CONSTRAINT uk_clientes_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE profissionais_servicos (
    id_profissional BIGINT NOT NULL,
    id_servico BIGINT NOT NULL,
    CONSTRAINT pk_profissionais_servicos PRIMARY KEY (
        id_profissional,
        id_servico
    ),
    CONSTRAINT fk_profissionais_servicos_profissional
        FOREIGN KEY (id_profissional)
        REFERENCES profissionais (id_profissional)
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_profissionais_servicos_servico
        FOREIGN KEY (id_servico)
        REFERENCES servicos (id_servico)
        ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE solicitacoes_servico (
    id_solicitacao BIGINT NOT NULL AUTO_INCREMENT,
    data_solicitacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_agendada DATETIME NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    descricao VARCHAR(1000) NOT NULL,
    observacoes VARCHAR(1000) NULL,
    id_cliente BIGINT NOT NULL,
    id_profissional BIGINT NOT NULL,
    id_servico BIGINT NOT NULL,
    CONSTRAINT pk_solicitacoes_servico PRIMARY KEY (id_solicitacao),
    CONSTRAINT ck_solicitacoes_status CHECK (
        status IN ('PENDENTE', 'ACEITA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA')
    ),
    CONSTRAINT ck_solicitacoes_data_agendada CHECK (
        data_agendada IS NULL OR data_agendada >= data_solicitacao
    ),
    CONSTRAINT fk_solicitacoes_cliente FOREIGN KEY (id_cliente)
        REFERENCES clientes (id_cliente)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_solicitacoes_profissional FOREIGN KEY (id_profissional)
        REFERENCES profissionais (id_profissional)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_solicitacoes_servico FOREIGN KEY (id_servico)
        REFERENCES servicos (id_servico)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE avaliacoes (
    id_avaliacao BIGINT NOT NULL AUTO_INCREMENT,
    nota TINYINT NOT NULL,
    comentario VARCHAR(1000) NULL,
    data_avaliacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_solicitacao BIGINT NOT NULL,
    CONSTRAINT pk_avaliacoes PRIMARY KEY (id_avaliacao),
    CONSTRAINT uk_avaliacoes_solicitacao UNIQUE (id_solicitacao),
    CONSTRAINT ck_avaliacoes_nota CHECK (nota BETWEEN 1 AND 5),
    CONSTRAINT fk_avaliacoes_solicitacao FOREIGN KEY (id_solicitacao)
        REFERENCES solicitacoes_servico (id_solicitacao)
        ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_servicos_categoria
    ON servicos (id_categoria);

CREATE INDEX idx_profissionais_local_disponibilidade
    ON profissionais (cidade, bairro, disponivel);

CREATE INDEX idx_solicitacoes_cliente
    ON solicitacoes_servico (id_cliente);

CREATE INDEX idx_solicitacoes_profissional
    ON solicitacoes_servico (id_profissional);

CREATE INDEX idx_solicitacoes_status_data
    ON solicitacoes_servico (status, data_solicitacao);
