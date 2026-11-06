package com.precisei.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.precisei.dto.AvaliacaoForm;
import com.precisei.model.Avaliacao;
import com.precisei.model.SolicitacaoServico;
import com.precisei.model.StatusSolicitacao;
import com.precisei.repository.AvaliacaoRepository;
import com.precisei.repository.SolicitacaoServicoRepository;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final SolicitacaoServicoRepository solicitacaoRepository;
    private final com.precisei.security.AcessoService acesso;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
            SolicitacaoServicoRepository solicitacaoRepository, com.precisei.security.AcessoService acesso) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.acesso = acesso;
    }

    @Transactional
    public Avaliacao cadastrar(Long solicitacaoId, AvaliacaoForm form) {
        SolicitacaoServico solicitacao = solicitacaoRepository.buscarParaAtualizar(solicitacaoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitação não encontrada."));
        acesso.exigirDono(com.precisei.security.TipoConta.CLIENTE, solicitacao.getCliente().getId());
        if (acesso.usuario().tipo() != com.precisei.security.TipoConta.CLIENTE) {
            throw new org.springframework.security.access.AccessDeniedException("A avaliação é exclusiva do cliente titular.");
        }
        if (form.getNota() < 1 || form.getNota() > 5 || (form.getComentario() != null && form.getComentario().length() > 1000)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Revise a nota e o comentário.");
        }
        if (solicitacao.getStatus() != StatusSolicitacao.CONCLUIDA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Somente solicitações concluídas podem ser avaliadas.");
        }
        if (avaliacaoRepository.existsBySolicitacaoId(solicitacaoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta solicitação já possui uma avaliação.");
        }
        return avaliacaoRepository.saveAndFlush(new Avaliacao(
                form.getNota(), form.getComentario(), solicitacao));
    }

    @Transactional(readOnly = true)
    public Map<Long, Double> mediasPorProfissional() {
        Map<Long, Double> medias = new HashMap<>();
        avaliacaoRepository.resumirPorProfissional().forEach(resumo ->
                medias.put(resumo.getProfissionalId(), resumo.getMedia()));
        return medias;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> quantidadesPorProfissional() {
        Map<Long, Long> quantidades = new HashMap<>();
        avaliacaoRepository.resumirPorProfissional().forEach(resumo ->
                quantidades.put(resumo.getProfissionalId(), resumo.getQuantidade()));
        return quantidades;
    }
}
