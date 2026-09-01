package com.precisei.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.precisei.dto.ServicoForm;
import com.precisei.model.Categoria;
import com.precisei.model.Servico;
import com.precisei.repository.ServicoRepository;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final CategoriaService categoriaService;

    public ServicoService(ServicoRepository servicoRepository,
            CategoriaService categoriaService) {
        this.servicoRepository = servicoRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<Servico> listarTodos() {
        return servicoRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public boolean existeDuplicado(ServicoForm form) {
        if (form.getNome() == null || form.getCategoriaId() == null) {
            return false;
        }
        if (form.getId() == null) {
            return servicoRepository.existsByNomeIgnoreCaseAndCategoriaId(
                    form.getNome().trim(), form.getCategoriaId());
        }
        return servicoRepository.existsByNomeIgnoreCaseAndCategoriaIdAndIdNot(
                form.getNome().trim(), form.getCategoriaId(), form.getId());
    }

    @Transactional(readOnly = true)
    public ServicoForm buscarFormulario(Long id) {
        Servico servico = buscarPorId(id);
        ServicoForm form = new ServicoForm();
        form.setId(servico.getId());
        form.setNome(servico.getNome());
        form.setDescricao(servico.getDescricao());
        form.setPrecoReferencia(servico.getPrecoReferencia());
        form.setCategoriaId(servico.getCategoria().getId());
        return form;
    }

    @Transactional
    public Servico salvar(ServicoForm form) {
        Categoria categoria = categoriaService.buscarPorId(form.getCategoriaId());
        Servico servico = form.getId() == null
                ? new Servico(form.getNome(), form.getDescricao(),
                        form.getPrecoReferencia(), categoria)
                : buscarPorId(form.getId());
        if (form.getId() != null) {
            servico.atualizar(form.getNome(), form.getDescricao(),
                    form.getPrecoReferencia(), categoria);
        }
        return servicoRepository.save(servico);
    }

    private Servico buscarPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Serviço não encontrado."));
    }
}
