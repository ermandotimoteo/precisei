-- Compartilhada pelas linhas histórica e operacional após a versão 2.
CREATE INDEX idx_solicitacoes_cliente_data ON solicitacoes_servico (id_cliente, data_solicitacao);
CREATE INDEX idx_solicitacoes_profissional_data ON solicitacoes_servico (id_profissional, data_solicitacao);
