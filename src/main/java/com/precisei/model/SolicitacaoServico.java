package com.precisei.model;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitacoes_servico")
public class SolicitacaoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitacao")
    private Long id;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_agendada")
    private LocalDateTime dataAgendada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacao status;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(length = 1000)
    private String observacoes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_profissional", nullable = false)
    private Profissional profissional;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_servico", nullable = false)
    private Servico servico;

    protected SolicitacaoServico() {
    }

    public SolicitacaoServico(LocalDateTime dataAgendada, String descricao,
            String observacoes, Cliente cliente, Profissional profissional,
            Servico servico) {
        this.dataSolicitacao = LocalDateTime.now();
        this.dataAgendada = dataAgendada;
        this.status = StatusSolicitacao.PENDENTE;
        this.descricao = descricao.trim();
        this.observacoes = observacoes == null ? null : observacoes.trim();
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = servico;
    }

    public Set<StatusSolicitacao> getProximosStatus() {
        return switch (status) {
            case PENDENTE -> Set.of(StatusSolicitacao.ACEITA, StatusSolicitacao.CANCELADA);
            case ACEITA -> Set.of(StatusSolicitacao.EM_ANDAMENTO, StatusSolicitacao.CANCELADA);
            case EM_ANDAMENTO -> Set.of(StatusSolicitacao.CONCLUIDA, StatusSolicitacao.CANCELADA);
            case CONCLUIDA, CANCELADA -> Set.of();
        };
    }

    public void alterarStatus(StatusSolicitacao novoStatus) {
        if (!getProximosStatus().contains(novoStatus)) {
            throw new IllegalStateException("Transição de status não permitida.");
        }
        this.status = novoStatus;
    }

    public Long getId() { return id; }
    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public LocalDateTime getDataAgendada() { return dataAgendada; }
    public StatusSolicitacao getStatus() { return status; }
    public String getDescricao() { return descricao; }
    public String getObservacoes() { return observacoes; }
    public Cliente getCliente() { return cliente; }
    public Profissional getProfissional() { return profissional; }
    public Servico getServico() { return servico; }
}
