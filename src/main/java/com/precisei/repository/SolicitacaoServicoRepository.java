package com.precisei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.precisei.model.SolicitacaoServico;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

public interface SolicitacaoServicoRepository extends JpaRepository<SolicitacaoServico, Long> {
    @EntityGraph(attributePaths = {"cliente", "profissional", "servico", "servico.categoria", "avaliacao"})
    List<SolicitacaoServico> findAllByOrderByDataSolicitacaoDesc();
}
