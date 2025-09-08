package co.com.crediya.api.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationManager(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token =  authentication.getCredentials().toString();

        return jwtProvider.getClaimsReactive(token)
                .log()
                .map(claims -> createAuthenticationToken(claims, token));
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(Claims claims, String token) {
        String username = claims.getSubject();
        List<String> roles = claims.containsKey("roles")
                ? (List<String>) claims.get("roles")
                : Collections.emptyList();

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new).toList();

        User principal = new User(username, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}