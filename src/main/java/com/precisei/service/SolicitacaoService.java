package com.precisei.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.precisei.dto.SolicitacaoForm;
import com.precisei.model.Cliente;
import com.precisei.model.Profissional;
import com.precisei.model.Servico;
import com.precisei.model.SolicitacaoServico;
import com.precisei.model.StatusSolicitacao;
import com.precisei.repository.ProfissionalRepository;
import com.precisei.repository.ServicoRepository;
import com.precisei.repository.SolicitacaoServicoRepository;

@Service
public class SolicitacaoService {

    private final SolicitacaoServicoRepository solicitacaoRepository;
    private final ClienteService clienteService;
    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;

    public SolicitacaoService(SolicitacaoServicoRepository solicitacaoRepository,
            ClienteService clienteService, ProfissionalRepository profissionalRepository,
            ServicoRepository servicoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.clienteService = clienteService;
        this.profissionalRepository = profissionalRepository;
        this.servicoRepository = servicoRepository;
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoServico> listarTodas() {
        return solicitacaoRepository.findAllByOrderByDataSolicitacaoDesc();
    }

    @Transactional(readOnly = true)
    public boolean profissionalPodeAtender(Long profissionalId, Long servicoId) {
        if (profissionalId == null || servicoId == null) {
            return true;
        }
        Profissional profissional = buscarProfissional(profissionalId);
        return profissional.isDisponivel() && profissional.getServicos().stream()
                .anyMatch(servico -> servico.getId().equals(servicoId));
    }

    @Transactional
    public SolicitacaoServico cadastrar(SolicitacaoForm form) {
        Cliente cliente = clienteService.buscarPorId(form.getClienteId());
        Profissional profissional = buscarProfissional(form.getProfissionalId());
        Servico servico = servicoRepository.findById(form.getServicoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Serviço não encontrado."));
        if (!profissional.isDisponivel() || profissional.getServicos().stream()
                .noneMatch(oferecido -> oferecido.getId().equals(servico.getId()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O profissional não está disponível para o serviço selecionado.");
        }
        SolicitacaoServico solicitacao = new SolicitacaoServico(form.getDataAgendada(),
                form.getDescricao(), form.getObservacoes(), cliente, profissional, servico);
        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void alterarStatus(Long id, StatusSolicitacao novoStatus) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitação não encontrada."));
        try {
            solicitacao.alterarStatus(novoStatus);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private Profissional buscarProfissional(Long id) {
        return profissionalRepository.findWithServicosById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Profissional não encontrado."));
    }
}
