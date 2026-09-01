package com.precisei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.precisei.model.Cliente;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findAllByOrderByNomeAsc();
    boolean existsByEmailIgnoreCase(String email);
}
