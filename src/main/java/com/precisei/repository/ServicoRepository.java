package com.precisei.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.precisei.model.Servico;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @EntityGraph(attributePaths = "categoria")
    List<Servico> findAllByOrderByNomeAsc();
}
