package com.precisei.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.precisei.model.Categoria;
import com.precisei.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public boolean existeComNome(String nome) {
        return nome != null && categoriaRepository.existsByNomeIgnoreCase(nome.trim());
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoria não encontrada."));
    }

    @Transactional
    public Categoria cadastrar(Categoria categoria) {
        categoria.setNome(categoria.getNome().trim());
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void garantirCategoriasPadrao(List<String> nomes) {
        for (String nome : nomes) {
            if (!categoriaRepository.existsByNomeIgnoreCase(nome)) {
                categoriaRepository.save(new Categoria(nome));
            }
        }
    }
}
