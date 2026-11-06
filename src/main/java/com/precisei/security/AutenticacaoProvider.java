package com.precisei.security;

import java.util.Locale;
import com.precisei.repository.ClienteRepository;
import com.precisei.repository.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication(type = org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET)
public class AutenticacaoProvider implements AuthenticationProvider {
    private final ClienteRepository clientes;
    private final ProfissionalRepository profissionais;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminHash;
    private final String dummyHash;
    public AutenticacaoProvider(ClienteRepository clientes, ProfissionalRepository profissionais, PasswordEncoder encoder,
            @Value("${app.security.admin-email:}") String adminEmail,
            @Value("${app.security.admin-password-hash:}") String adminHash) {
        this.clientes = clientes; this.profissionais = profissionais; this.encoder = encoder;
        this.adminEmail = adminEmail; this.adminHash = adminHash;
        this.dummyHash = encoder.encode(java.util.UUID.randomUUID().toString());
    }
    @Override @Transactional
    public Authentication authenticate(Authentication authentication) {
        String[] dados = authentication.getName().split("\\|", 2);
        String senha = String.valueOf(authentication.getCredentials());
        if (dados.length != 2 || senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw erro();
        String email = dados[1].trim().toLowerCase(Locale.ROOT);
        UsuarioAutenticado principal = null;
        String hash = dummyHash;
        switch (dados[0]) {
            case "CLIENTE" -> {
                var conta = clientes.findByEmailIgnoreCase(email);
                if (conta.isPresent()) { var c = conta.get(); hash = c.getSenhaHash(); principal = new UsuarioAutenticado(c.getId(), TipoConta.CLIENTE, c.getNome()); }
            }
            case "PROFISSIONAL" -> {
                var conta = profissionais.findByEmailIgnoreCase(email);
                if (conta.isPresent()) { var c = conta.get(); hash = c.getSenhaHash(); principal = new UsuarioAutenticado(c.getId(), TipoConta.PROFISSIONAL, c.getNome()); }
            }
            case "MANUTENCAO" -> {
                if (!adminEmail.isBlank() && !adminHash.isBlank() && adminEmail.equalsIgnoreCase(email)) {
                    hash = adminHash; principal = new UsuarioAutenticado(0L, TipoConta.MANUTENCAO, "Manutenção");
                }
            }
            default -> { }
        }
        boolean confere;
        try { confere = encoder.matches(senha, hash); } catch (IllegalArgumentException ex) { confere = false; }
        if (principal == null || !confere) throw erro();
        if (principal.tipo() != TipoConta.MANUTENCAO && encoder.upgradeEncoding(hash)) {
            String novoHash = encoder.encode(senha);
            if (principal.tipo() == TipoConta.CLIENTE) clientes.findById(principal.id()).orElseThrow().alterarSenha(novoHash);
            else profissionais.findById(principal.id()).orElseThrow().alterarSenha(novoHash);
        }
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }
    private BadCredentialsException erro() { return new BadCredentialsException("Credenciais inválidas."); }
    @Override public boolean supports(Class<?> type) { return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type); }
}
