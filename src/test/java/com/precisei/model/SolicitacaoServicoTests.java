package com.precisei.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

class SolicitacaoServicoTests {

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("transicoes")
    void verificaTodasAsSaidasDeCadaEstado(StatusSolicitacao origem, StatusSolicitacao alvo) {
        var pendente = criarSolicitacao();
        if (origem == StatusSolicitacao.CANCELADA) pendente.alterarStatus(origem);
        else if (origem != StatusSolicitacao.PENDENTE) {
            pendente.alterarStatus(StatusSolicitacao.ACEITA);
            if (origem != StatusSolicitacao.ACEITA) pendente.alterarStatus(StatusSolicitacao.EM_ANDAMENTO);
            if (origem == StatusSolicitacao.CONCLUIDA) pendente.alterarStatus(origem);
        }
        var permitidos = java.util.Map.of(
                StatusSolicitacao.PENDENTE, Set.of(StatusSolicitacao.ACEITA, StatusSolicitacao.CANCELADA),
                StatusSolicitacao.ACEITA, Set.of(StatusSolicitacao.EM_ANDAMENTO, StatusSolicitacao.CANCELADA),
                StatusSolicitacao.EM_ANDAMENTO, Set.of(StatusSolicitacao.CONCLUIDA, StatusSolicitacao.CANCELADA),
                StatusSolicitacao.CONCLUIDA, Set.<StatusSolicitacao>of(),
                StatusSolicitacao.CANCELADA, Set.<StatusSolicitacao>of()).get(origem);
        assertEquals(permitidos, pendente.getProximosStatus());
        if (permitidos.contains(alvo)) {
            pendente.alterarStatus(alvo);assertEquals(alvo,pendente.getStatus());
        } else {
            assertThrows(IllegalStateException.class,() -> pendente.alterarStatus(alvo));
            assertEquals(origem, pendente.getStatus());
        }
    }

    static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> transicoes() {
        return java.util.Arrays.stream(StatusSolicitacao.values()).flatMap(origem ->
                java.util.Arrays.stream(StatusSolicitacao.values()).map(alvo ->
                        org.junit.jupiter.params.provider.Arguments.of(origem, alvo)));
    }

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
