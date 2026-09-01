package com.precisei.model;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "profissionais")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profissional")
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 100)
    private String bairro;

    @Column(nullable = false)
    private boolean disponivel;

    @Column(name = "foto_perfil", length = 255)
    private String fotoPerfil;

    @ManyToMany
    @JoinTable(name = "profissionais_servicos",
            joinColumns = @JoinColumn(name = "id_profissional"),
            inverseJoinColumns = @JoinColumn(name = "id_servico"))
    private Set<Servico> servicos = new LinkedHashSet<>();

    protected Profissional() {
    }

    public Profissional(String nome, String telefone, String email, String senhaHash,
            String descricao, String cidade, String bairro, boolean disponivel,
            Set<Servico> servicos) {
        this.senhaHash = senhaHash;
        atualizar(nome, telefone, email, descricao, cidade, bairro, disponivel, servicos);
    }

    public void atualizar(String nome, String telefone, String email, String descricao,
            String cidade, String bairro, boolean disponivel, Set<Servico> servicos) {
        this.nome = nome.trim();
        this.telefone = telefone.trim();
        this.email = email.trim().toLowerCase();
        this.descricao = descricao == null ? null : descricao.trim();
        this.cidade = cidade.trim();
        this.bairro = bairro.trim();
        this.disponivel = disponivel;
        this.servicos.clear();
        this.servicos.addAll(servicos);
    }

    public void alterarSenha(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getDescricao() { return descricao; }
    public String getCidade() { return cidade; }
    public String getBairro() { return bairro; }
    public boolean isDisponivel() { return disponivel; }
    public String getFotoPerfil() { return fotoPerfil; }
    public Set<Servico> getServicos() { return servicos; }
}
