package com.precisei.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.precisei.dto.ClienteForm;
import com.precisei.model.Cliente;
import com.precisei.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.precisei.security.AcessoService acesso;

    public ClienteService(ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder, com.precisei.security.AcessoService acesso) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        if (acesso.manutencao()) return clienteRepository.findAllByOrderByNomeAsc();
        var user = acesso.opcional();
        return user != null && user.tipo() == com.precisei.security.TipoConta.CLIENTE
                ? clienteRepository.findById(user.id()).map(List::of).orElseGet(List::of) : List.of();
    }

    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return email != null && clienteRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Transactional
    public Cliente cadastrar(ClienteForm form) {
        com.precisei.security.PoliticaSenha.exigir(form.getSenha());
        Cliente cliente = new Cliente(form.getNome(), form.getTelefone(), form.getEmail(),
                passwordEncoder.encode(form.getSenha()));
        return clienteRepository.saveAndFlush(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado."));
    }
}
