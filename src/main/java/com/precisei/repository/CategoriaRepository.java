package com.precisei.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.precisei.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
