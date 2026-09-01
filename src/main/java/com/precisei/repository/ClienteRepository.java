package com.precisei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.precisei.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
