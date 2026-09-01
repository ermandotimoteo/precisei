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

    public ClienteService(ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return email != null && clienteRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Transactional
    public Cliente cadastrar(ClienteForm form) {
        Cliente cliente = new Cliente(form.getNome(), form.getTelefone(), form.getEmail(),
                passwordEncoder.encode(form.getSenha()));
        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado."));
    }
}
