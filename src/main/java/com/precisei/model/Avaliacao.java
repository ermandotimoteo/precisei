package com.precisei.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Long id;

    @Column(nullable = false)
    private byte nota;

    @Column(length = 1000)
    private String comentario;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDateTime dataAvaliacao;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_solicitacao", nullable = false, unique = true)
    private SolicitacaoServico solicitacao;

    protected Avaliacao() {
    }

    public Avaliacao(byte nota, String comentario, SolicitacaoServico solicitacao) {
        this.nota = nota;
        this.comentario = comentario == null || comentario.isBlank()
                ? null : comentario.trim();
        this.dataAvaliacao = LocalDateTime.now();
        this.solicitacao = solicitacao;
    }

    public Long getId() { return id; }
    public byte getNota() { return nota; }
    public String getComentario() { return comentario; }
    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public SolicitacaoServico getSolicitacao() { return solicitacao; }
}
