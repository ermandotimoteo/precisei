package com.precisei.model;

import java.time.LocalDateTime;

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
