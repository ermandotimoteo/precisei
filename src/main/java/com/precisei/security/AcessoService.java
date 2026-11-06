package com.precisei.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AcessoService {
    public UsuarioAutenticado opcional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof UsuarioAutenticado user ? user : null;
    }
    public UsuarioAutenticado usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UsuarioAutenticado user) {
            return user;
        }
        throw new AccessDeniedException("Entre em sua conta para continuar.");
    }
    public boolean manutencao() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANUTENCAO"));
    }
    public void exigirDono(TipoConta tipo, Long id) {
        if (manutencao()) return;
        UsuarioAutenticado user = usuario();
        if (user.tipo() != tipo || !user.id().equals(id)) {
            throw new AccessDeniedException("Você não tem acesso a este registro.");
        }
    }
    public Long clienteAtual() {
        UsuarioAutenticado user = usuario();
        if (user.tipo() != TipoConta.CLIENTE) throw new AccessDeniedException("Esta ação é exclusiva de clientes.");
        return user.id();
    }
    public boolean profissionalProprio(Long id) {
        UsuarioAutenticado user = opcional();
        return user != null && user.tipo() == TipoConta.PROFISSIONAL && user.id().equals(id);
    }
    public void exigirPedido(com.precisei.model.SolicitacaoServico pedido) {
        if (manutencao()) return;
        UsuarioAutenticado user = usuario();
        boolean dono = user.tipo() == TipoConta.CLIENTE && user.id().equals(pedido.getCliente().getId());
        boolean prestador = user.tipo() == TipoConta.PROFISSIONAL && user.id().equals(pedido.getProfissional().getId());
        if (!dono && !prestador) throw new AccessDeniedException("Você não tem acesso a este atendimento.");
    }
    public java.util.Set<com.precisei.model.StatusSolicitacao> proximosStatus(com.precisei.model.SolicitacaoServico pedido) {
        exigirPedido(pedido);
        UsuarioAutenticado user = usuario();
        if (user.tipo() == TipoConta.CLIENTE) {
            return pedido.getProximosStatus().contains(com.precisei.model.StatusSolicitacao.CANCELADA)
                    ? java.util.Set.of(com.precisei.model.StatusSolicitacao.CANCELADA) : java.util.Set.of();
        }
        return pedido.getProximosStatus();
    }
    public boolean podeAvaliar(com.precisei.model.SolicitacaoServico pedido) {
        UsuarioAutenticado user = opcional();
        return user != null && user.tipo() == TipoConta.CLIENTE
                && user.id().equals(pedido.getCliente().getId()) && pedido.isAvaliavel();
    }
}
