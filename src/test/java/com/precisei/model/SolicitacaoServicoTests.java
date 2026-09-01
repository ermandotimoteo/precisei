package com.precisei.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

class SolicitacaoServicoTests {

    @Test
    void deveRespeitarFluxoDeStatus() {
        SolicitacaoServico solicitacao = criarSolicitacao();

        solicitacao.alterarStatus(StatusSolicitacao.ACEITA);
        solicitacao.alterarStatus(StatusSolicitacao.EM_ANDAMENTO);
        solicitacao.alterarStatus(StatusSolicitacao.CONCLUIDA);

        assertEquals(StatusSolicitacao.CONCLUIDA, solicitacao.getStatus());
        assertThrows(IllegalStateException.class,
                () -> solicitacao.alterarStatus(StatusSolicitacao.PENDENTE));
    }

    @Test
    void devePermitirCancelarSolicitacaoPendente() {
        SolicitacaoServico solicitacao = criarSolicitacao();
        solicitacao.alterarStatus(StatusSolicitacao.CANCELADA);
        assertEquals(StatusSolicitacao.CANCELADA, solicitacao.getStatus());
    }

    private SolicitacaoServico criarSolicitacao() {
        Categoria categoria = new Categoria("Elétrica");
        Servico servico = new Servico("Tomada", "Troca de tomada", null, categoria);
        Profissional profissional = new Profissional("Carlos", "9999-9999",
                "carlos@teste.local", "hash", null, "João Pessoa", "Centro",
                true, Set.of(servico));
        Cliente cliente = new Cliente("Maria", "9888-8888", "maria@teste.local", "hash");
        return new SolicitacaoServico(null, "Trocar tomada", null,
                cliente, profissional, servico);
    }
}
