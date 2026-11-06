package com.precisei.service;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.precisei.dto.ProfissionalForm;
import com.precisei.model.Profissional;
import com.precisei.model.Servico;
import com.precisei.repository.ProfissionalRepository;
import com.precisei.repository.ServicoRepository;

@Service
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.precisei.security.AcessoService acesso;
    private final org.springframework.security.core.session.SessionRegistry sessoes;

    public ProfissionalService(ProfissionalRepository profissionalRepository,
            ServicoRepository servicoRepository, PasswordEncoder passwordEncoder, com.precisei.security.AcessoService acesso,
            org.springframework.security.core.session.SessionRegistry sessoes) {
        this.profissionalRepository = profissionalRepository;
        this.servicoRepository = servicoRepository;
        this.passwordEncoder = passwordEncoder;
        this.acesso = acesso;
        this.sessoes = sessoes;
    }

    @Transactional(readOnly = true)
    public List<Profissional> listarTodos() {
        return profissionalRepository.findDistinctByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public boolean existeEmailDuplicado(ProfissionalForm form) {
        if (form.getEmail() == null) {
            return false;
        }
        if (form.getId() == null) {
            return profissionalRepository.existsByEmailIgnoreCase(form.getEmail().trim());
        }
        return profissionalRepository.existsByEmailIgnoreCaseAndIdNot(
                form.getEmail().trim(), form.getId());
    }

    @Transactional(readOnly = true)
    public ProfissionalForm buscarFormulario(Long id) {
        acesso.exigirDono(com.precisei.security.TipoConta.PROFISSIONAL, id);
        Profissional profissional = buscarPorId(id);
        ProfissionalForm form = new ProfissionalForm();
        form.setId(profissional.getId());
        form.setNome(profissional.getNome());
        form.setTelefone(profissional.getTelefone());
        form.setEmail(profissional.getEmail());
        form.setDescricao(profissional.getDescricao());
        form.setCidade(profissional.getCidade());
        form.setBairro(profissional.getBairro());
        form.setDisponivel(profissional.isDisponivel());
        form.setServicoIds(profissional.getServicos().stream().map(Servico::getId).toList());
        return form;
    }

    @Transactional
    public Profissional salvar(ProfissionalForm form) {
        Set<Servico> servicos = new LinkedHashSet<>(
                servicoRepository.findAllById(form.getServicoIds()));
        if (servicos.size() != new LinkedHashSet<>(form.getServicoIds()).size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Um dos serviços selecionados não existe.");
        }

        if (form.getId() == null) {
            com.precisei.security.PoliticaSenha.exigir(form.getSenha());
            Profissional profissional = new Profissional(
                    form.getNome(), form.getTelefone(), form.getEmail(),
                    passwordEncoder.encode(form.getSenha()), form.getDescricao(),
                    form.getCidade(), form.getBairro(), form.isDisponivel(), servicos);
            return profissionalRepository.saveAndFlush(profissional);
        }

        acesso.exigirDono(com.precisei.security.TipoConta.PROFISSIONAL, form.getId());
        Profissional profissional = buscarPorId(form.getId());
        if (form.getSenha() != null && !form.getSenha().isBlank()) {
            com.precisei.security.PoliticaSenha.exigir(form.getSenha());
            if (form.getSenhaAtual() == null || !passwordEncoder.matches(form.getSenhaAtual(), profissional.getSenhaHash())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirme sua senha atual para alterar a senha.");
            }
        }
        form.setEmail(profissional.getEmail());
        profissional.atualizar(form.getNome(), form.getTelefone(), form.getEmail(),
                form.getDescricao(), form.getCidade(), form.getBairro(),
                form.isDisponivel(), servicos);
        if (form.getSenha() != null && !form.getSenha().isBlank()) {
            profissional.alterarSenha(passwordEncoder.encode(form.getSenha()));
            sessoes.getAllPrincipals().stream()
                    .filter(p -> p instanceof com.precisei.security.UsuarioAutenticado u
                            && u.tipo() == com.precisei.security.TipoConta.PROFISSIONAL && u.id().equals(form.getId()))
                    .forEach(p -> sessoes.getAllSessions(p, false).forEach(org.springframework.security.core.session.SessionInformation::expireNow));
        }
        return profissionalRepository.save(profissional);
    }

    private Profissional buscarPorId(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Profissional não encontrado."));
    }
}
