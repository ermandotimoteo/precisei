package com.precisei.service;

import com.precisei.dto.SolicitacaoForm;
import com.precisei.model.*;
import com.precisei.repository.*;
import com.precisei.security.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceTests {
    @Mock SolicitacaoServicoRepository repository;
    @Mock ClienteService clientes;
    @Mock ProfissionalRepository profissionais;
    @Mock ServicoRepository servicos;
    @Mock AcessoService acesso;
    @InjectMocks SolicitacaoService service;
    @Test void clienteSoListaPropriosPedidos() {
        when(acesso.usuario()).thenReturn(new UsuarioAutenticado(7L, TipoConta.CLIENTE,"Teste"));
        when(repository.findByClienteIdOrderByDataSolicitacaoDesc(7L)).thenReturn(List.of());
        service.listarTodas();verify(repository).findByClienteIdOrderByDataSolicitacaoDesc(7L);
        verify(repository,never()).findAllByOrderByDataSolicitacaoDesc();
    }
    @Test void cadastroIgnoraClienteForjado() {
        var form = formulario(); form.setClienteId(999L);
        var profissional = mock(Profissional.class); var servico = mock(Servico.class);
        when(acesso.clienteAtual()).thenReturn(7L);
        when(profissionais.findWithServicosById(2L)).thenReturn(Optional.of(profissional));
        when(servicos.findById(3L)).thenReturn(Optional.of(servico));
        when(servico.getId()).thenReturn(3L);when(profissional.isDisponivel()).thenReturn(true);
        when(profissional.getServicos()).thenReturn(Set.of(servico));
        service.cadastrar(form);verify(clientes).buscarPorId(7L);verify(clientes,never()).buscarPorId(999L);
    }
    @Test void indisponibilidadeNaoPersiste() {
        var form = formulario();var profissional = mock(Profissional.class);
        when(profissionais.findWithServicosById(2L)).thenReturn(Optional.of(profissional));
        when(servicos.findById(3L)).thenReturn(Optional.of(mock(Servico.class)));
        assertThrows(ResponseStatusException.class,() -> service.cadastrar(form));verify(repository,never()).save(any());
    }
    private SolicitacaoForm formulario() { var f = new SolicitacaoForm();f.setProfissionalId(2L);f.setServicoId(3L);f.setDescricao("Atendimento");return f; }
}
