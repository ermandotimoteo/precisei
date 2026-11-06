package com.precisei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.precisei.model.SolicitacaoServico;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

public interface SolicitacaoServicoRepository extends JpaRepository<SolicitacaoServico, Long> {
    @EntityGraph(attributePaths = {"cliente", "profissional", "servico", "servico.categoria", "avaliacao"})
    List<SolicitacaoServico> findAllByOrderByDataSolicitacaoDesc();

    @EntityGraph(attributePaths = {"cliente", "profissional", "servico", "servico.categoria", "avaliacao"})
    List<SolicitacaoServico> findByClienteIdOrderByDataSolicitacaoDesc(Long id);

    @EntityGraph(attributePaths = {"cliente", "profissional", "servico", "servico.categoria", "avaliacao"})
    List<SolicitacaoServico> findByProfissionalIdOrderByDataSolicitacaoDesc(Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from SolicitacaoServico s where s.id = :id")
    java.util.Optional<SolicitacaoServico> buscarParaAtualizar(@org.springframework.data.repository.query.Param("id") Long id);
}
