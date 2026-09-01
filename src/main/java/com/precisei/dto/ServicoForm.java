package com.precisei.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServicoForm {

    private Long id;

    @NotBlank(message = "Informe o nome do serviço.")
    @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "Informe a descrição do serviço.")
    @Size(max = 500, message = "A descrição deve possuir no máximo 500 caracteres.")
    private String descricao;

    @DecimalMin(value = "0.00", message = "O preço não pode ser negativo.")
    private BigDecimal precoReferencia;

    @NotNull(message = "Selecione uma categoria.")
    private Long categoriaId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPrecoReferencia() { return precoReferencia; }
    public void setPrecoReferencia(BigDecimal precoReferencia) { this.precoReferencia = precoReferencia; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
}
