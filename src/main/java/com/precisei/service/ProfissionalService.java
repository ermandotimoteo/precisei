package com.precisei.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.precisei.model.Profissional;
import com.precisei.repository.ProfissionalRepository;

@Service
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;

    public ProfissionalService(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    @Transactional(readOnly = true)
    public List<Profissional> listarTodos() {
        return profissionalRepository.findDistinctByOrderByNomeAsc();
    }
}
