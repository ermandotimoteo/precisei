package com.precisei.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.precisei.model.Profissional;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    @EntityGraph(attributePaths = {"servicos", "servicos.categoria"})
    List<Profissional> findDistinctByOrderByNomeAsc();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @EntityGraph(attributePaths = "servicos")
    Optional<Profissional> findWithServicosById(Long id);
}
