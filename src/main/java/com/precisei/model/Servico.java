package com.precisei.model;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servico")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(name = "preco_referencia", precision = 10, scale = 2)
    private BigDecimal precoReferencia;

    @Column(length = 255)
    private String imagem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToMany(mappedBy = "servicos")
    private Set<Profissional> profissionais = new LinkedHashSet<>();

    protected Servico() {
    }

    public Servico(String nome, String descricao, BigDecimal precoReferencia,
            Categoria categoria) {
        atualizar(nome, descricao, precoReferencia, categoria);
    }

    public void atualizar(String nome, String descricao, BigDecimal precoReferencia,
            Categoria categoria) {
        this.nome = nome.trim();
        this.descricao = descricao.trim();
        this.precoReferencia = precoReferencia;
        this.categoria = categoria;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoReferencia() { return precoReferencia; }
    public String getImagem() { return imagem; }
    public Categoria getCategoria() { return categoria; }
    public Set<Profissional> getProfissionais() { return profissionais; }
}
