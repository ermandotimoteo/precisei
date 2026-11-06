package com.precisei.security;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** A identidade inclui o tipo: cliente 1 e profissional 1 não são a mesma conta. */
public record UsuarioAutenticado(Long id, TipoConta tipo, String nome) implements UserDetails {
    @Serial private static final long serialVersionUID = 1L;
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + tipo.name()));
    }
    @Override public String getUsername() { return tipo.name() + ":" + id; }
    @Override public String getPassword() { return null; }
}
