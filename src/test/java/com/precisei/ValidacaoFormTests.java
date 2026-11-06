package com.precisei;

import com.precisei.dto.*;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ValidacaoFormTests {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    @AfterAll static void fechar() { FACTORY.close(); }
    @Test void clienteRespeitaLimitesENaoAceitaEmailInvalido() {
        var f=new ClienteForm();f.setNome("x".repeat(120));f.setEmail("a@example.invalid");f.setTelefone("0".repeat(20));
        assertTrue(FACTORY.getValidator().validate(f).isEmpty());
        f.setNome("x".repeat(121));f.setEmail("invalido");f.setTelefone("0".repeat(21));
        assertEquals(3,FACTORY.getValidator().validate(f).size());
    }
    @Test void solicitacaoPermiteDataAusenteENaoPassado() {
        var f=new SolicitacaoForm();f.setProfissionalId(1L);f.setServicoId(2L);f.setDescricao("x".repeat(1000));
        assertTrue(FACTORY.getValidator().validate(f).isEmpty());
        f.setDataAgendada(LocalDateTime.now().minusDays(1));f.setDescricao("x".repeat(1001));
        assertEquals(2,FACTORY.getValidator().validate(f).size());
    }
    @Test void servicoVerificaDominioDoPrecoAntesDoBanco() {
        var f=new ServicoForm();f.setNome("Serviço");f.setDescricao("Descrição");f.setCategoriaId(1L);
        f.setPrecoReferencia(BigDecimal.ZERO);assertTrue(FACTORY.getValidator().validate(f).isEmpty());
        f.setPrecoReferencia(new BigDecimal("-0.01"));assertFalse(FACTORY.getValidator().validate(f).isEmpty());
        f.setPrecoReferencia(new BigDecimal("100000000.00"));assertFalse(FACTORY.getValidator().validate(f).isEmpty());
        f.setPrecoReferencia(new BigDecimal("1.234"));assertFalse(FACTORY.getValidator().validate(f).isEmpty());
    }
    @Test void avaliacaoLimitaNotaEComentario() {
        var f=new AvaliacaoForm();f.setNota((byte)1);f.setComentario("x".repeat(1000));
        assertTrue(FACTORY.getValidator().validate(f).isEmpty());
        f.setNota((byte)6);f.setComentario("x".repeat(1001));assertEquals(2,FACTORY.getValidator().validate(f).size());
    }
}
