package co.com.crediya.api.security;

import co.com.crediya.api.enums.AuthenticationErrorEnum;
import co.com.crediya.api.exceptions.ValidateAuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.logging.Logger;

@Component
public class JwtProvider {

    private static final Logger LOGGER = Logger.getLogger(JwtProvider.class.getName());

    @Value("${jwt.secret}")
    private String secret;

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(getKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Mono<Claims> getClaimsReactive(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey(secret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Mono.just(claims);
        } catch (ExpiredJwtException e) {
            LOGGER.severe("token expired");
            return Mono.error(new ValidateAuthException(AuthenticationErrorEnum.TOKEN_EXPIRED.name(), HttpStatus.UNAUTHORIZED));
        } catch (UnsupportedJwtException e) {
            LOGGER.severe("token unsupported");
            return Mono.error(new ValidateAuthException(AuthenticationErrorEnum.TOKEN_UNSUPPORTED.name(), HttpStatus.UNAUTHORIZED));
        } catch (MalformedJwtException e) {
            LOGGER.severe("token malformed");
            return Mono.error(new ValidateAuthException(AuthenticationErrorEnum.TOKEN_MALFORMED.name(), HttpStatus.UNAUTHORIZED));
        } catch (SignatureException e) {
            LOGGER.severe("bad signature");
            return Mono.error(new ValidateAuthException(AuthenticationErrorEnum.BAD_SIGNATURE.name(), HttpStatus.UNAUTHORIZED));
        } catch (IllegalArgumentException e) {
            LOGGER.severe("illegal args");
            return Mono.error(new ValidateAuthException(AuthenticationErrorEnum.ILLEGAL_ARGS.name(), HttpStatus.UNAUTHORIZED));
        }
    }

    private SecretKey getKey(String secret) {
        byte[] secretBytes = Decoders.BASE64URL.decode(secret);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}