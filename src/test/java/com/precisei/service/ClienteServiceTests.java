package com.precisei.service;

import com.precisei.dto.ClienteForm;
import com.precisei.model.Cliente;
import com.precisei.repository.ClienteRepository;
import com.precisei.security.AcessoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTests {
    @Mock ClienteRepository repository;
    @Mock PasswordEncoder encoder;
    @Mock AcessoService acesso;
    @InjectMocks ClienteService service;
    @Test void anonimoNuncaListaContatos() { assertTrue(service.listarTodos().isEmpty()); verifyNoInteractions(repository); }
    @Test void cadastroUsaHashEFlushParaConflitos() {
        ClienteForm form = new ClienteForm();form.setNome("Teste");form.setTelefone("00000000");form.setEmail("a@example.invalid");form.setSenha("uma-senha-segura");
        when(encoder.encode(form.getSenha())).thenReturn("hash");
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        Cliente cliente = service.cadastrar(form);
        assertEquals("hash",cliente.getSenhaHash());assertNotEquals(form.getSenha(),cliente.getSenhaHash());
        verify(repository).saveAndFlush(any());
    }
    @Test void senhaInvalidaNaoChegaAoBanco() {
        ClienteForm form = new ClienteForm(); form.setSenha("curta");
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(form));verifyNoInteractions(repository,encoder);
    }
}
