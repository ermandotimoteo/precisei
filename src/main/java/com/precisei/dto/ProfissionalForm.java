package com.precisei.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class ProfissionalForm {

    private Long id;

    @NotBlank(message = "Informe o nome do profissional.")
    @Size(max = 120, message = "O nome deve possuir no máximo 120 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o telefone.")
    @Size(max = 20, message = "O telefone deve possuir no máximo 20 caracteres.")
    private String telefone;

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 160, message = "O e-mail deve possuir no máximo 160 caracteres.")
    private String email;

    private String senha;

    @Size(max = 1000, message = "A descrição deve possuir no máximo 1000 caracteres.")
    private String descricao;

    @NotBlank(message = "Informe a cidade.")
    @Size(max = 100, message = "A cidade deve possuir no máximo 100 caracteres.")
    private String cidade;

    @NotBlank(message = "Informe o bairro.")
    @Size(max = 100, message = "O bairro deve possuir no máximo 100 caracteres.")
    private String bairro;

    private boolean disponivel;

    @NotEmpty(message = "Selecione ao menos um serviço.")
    private List<Long> servicoIds = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
    public List<Long> getServicoIds() { return servicoIds; }
    public void setServicoIds(List<Long> servicoIds) { this.servicoIds = servicoIds; }
}
