package co.com.crediya.api.security;


import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.exceptions.FundException;
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
        return Mono.just(authentication)
                .map(auth -> jwtProvider.getClaims(auth.getCredentials().toString()))
                .log()
                .onErrorResume(e -> Mono.error(new FundException(FundErrorEnum.INVALID_TOKEN)))
                .map(this::createAuthenticationToken);
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(Claims claims) {
        String username = claims.getSubject();
        List<String> roles = claims.containsKey("roles")
                ?(List<String>) claims.get("roles")
                : Collections.emptyList();

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new).toList();


        User principal = new User(username, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);

    }

}