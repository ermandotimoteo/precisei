package com.precisei.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.precisei.model.Profissional;
import com.precisei.model.Categoria;
import com.precisei.model.Servico;
import com.precisei.dto.ProfissionalForm;
import com.precisei.repository.ProfissionalRepository;
import com.precisei.repository.ServicoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTests {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfissionalService profissionalService;

    @Test
    void deveListarProfissionaisComSeusServicos() {
        List<Profissional> profissionais = List.of();
        when(profissionalRepository.findDistinctByOrderByNomeAsc()).thenReturn(profissionais);

        assertSame(profissionais, profissionalService.listarTodos());
    }

    @Test
    void deveCriptografarSenhaAoCadastrarProfissional() {
        ProfissionalForm form = new ProfissionalForm();
        form.setNome("Profissional Teste");
        form.setTelefone("(83) 90000-0000");
        form.setEmail("teste@precisei.local");
        form.setSenha("senha-segura");
        form.setCidade("João Pessoa");
        form.setBairro("Centro");
        form.setServicoIds(List.of(1L));
        Servico servico = new Servico("Teste", "Serviço de teste", null,
                new Categoria("Elétrica"));
        when(servicoRepository.findAllById(form.getServicoIds())).thenReturn(List.of(servico));
        when(passwordEncoder.encode("senha-segura")).thenReturn("hash-seguro");

        profissionalService.salvar(form);

        verify(passwordEncoder).encode("senha-segura");
        verify(profissionalRepository).save(any(Profissional.class));
    }
}
