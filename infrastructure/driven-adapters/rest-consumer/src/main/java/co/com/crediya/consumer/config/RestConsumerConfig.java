package co.com.crediya.consumer.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
public class RestConsumerConfig {

    @Bean
    public WebClient webClientUser(WebClient.Builder builder,
                                         @Value("${adapter.restconsumer.user.host}") String host,
                                         @Value("${adapter.restconsumer.timeout}") int timeout) {

        System.out.println("AUTH HOST CONFIGURED: " + host);

        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("AUTH_API_URL cannot be null or empty");
        }

        return builder
                .baseUrl(host)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(getClientHttpConnector(timeout))
                .filter(authorizationHeaderFilter())
                .build();
    }

    private ClientHttpConnector getClientHttpConnector(int timeout) {
        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(Boolean.TRUE)
                .keepAlive(Boolean.TRUE)
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                }));
    }

    private ExchangeFilterFunction authorizationHeaderFilter(){
        return ((request, next) ->
                ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .filter(Authentication::isAuthenticated)
                        .flatMap(authentication -> {
                            Object credentials = authentication.getCredentials();
                            if (credentials instanceof String token && !token.isBlank()){
                                var newRequest = ClientRequest.from(request)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                        .build();
                                return next.exchange(newRequest);
                            }
                            return next.exchange(request);
                        })
                        .switchIfEmpty(next.exchange(request))
        );
    }

}
