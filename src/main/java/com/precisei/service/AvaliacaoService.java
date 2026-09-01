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

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
            SolicitacaoServicoRepository solicitacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional
    public Avaliacao cadastrar(Long solicitacaoId, AvaliacaoForm form) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitação não encontrada."));
        if (solicitacao.getStatus() != StatusSolicitacao.CONCLUIDA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Somente solicitações concluídas podem ser avaliadas.");
        }
        if (avaliacaoRepository.existsBySolicitacaoId(solicitacaoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta solicitação já possui uma avaliação.");
        }
        return avaliacaoRepository.save(new Avaliacao(
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
