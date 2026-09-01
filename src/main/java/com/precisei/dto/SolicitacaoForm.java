package com.precisei.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SolicitacaoForm {

    @NotNull(message = "Selecione um cliente.")
    private Long clienteId;

    @NotNull(message = "Selecione um profissional.")
    private Long profissionalId;

    @NotNull(message = "Selecione um serviço.")
    private Long servicoId;

    @FutureOrPresent(message = "A data agendada não pode estar no passado.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataAgendada;

    @NotBlank(message = "Descreva o atendimento necessário.")
    @Size(max = 1000, message = "A descrição deve possuir no máximo 1000 caracteres.")
    private String descricao;

    @Size(max = 1000, message = "As observações devem possuir no máximo 1000 caracteres.")
    private String observacoes;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getProfissionalId() { return profissionalId; }
    public void setProfissionalId(Long profissionalId) { this.profissionalId = profissionalId; }
    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public LocalDateTime getDataAgendada() { return dataAgendada; }
    public void setDataAgendada(LocalDateTime dataAgendada) { this.dataAgendada = dataAgendada; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
