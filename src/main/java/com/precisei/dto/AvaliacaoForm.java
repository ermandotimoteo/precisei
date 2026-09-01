package com.precisei.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class AvaliacaoForm {

    @Min(value = 1, message = "Escolha uma nota entre 1 e 5.")
    @Max(value = 5, message = "Escolha uma nota entre 1 e 5.")
    private byte nota;

    @Size(max = 1000, message = "O comentário deve possuir no máximo 1000 caracteres.")
    private String comentario;

    public byte getNota() { return nota; }
    public void setNota(byte nota) { this.nota = nota; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
