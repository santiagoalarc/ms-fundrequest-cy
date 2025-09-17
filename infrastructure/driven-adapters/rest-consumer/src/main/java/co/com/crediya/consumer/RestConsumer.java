package co.com.crediya.consumer;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class RestConsumer implements UserRestService {

    @Qualifier("webClientUser")
    private final WebClient client;

    private final ObjectMapper mapper;
    @Value("${adapter.restconsumer.user.url}")
    private String urlFindByDocument;

    @Value("${adapter.restconsumer.user.url-users-email}")
    private String urlFindByEmails;

    @Value("${adapter.restconsumer.retry}")
    private Integer retry;

    private final Logger log = Logger.getLogger(RestConsumer.class.getName());


    @Override
    public Mono<User> findUserByDocumentNumber(String documentNumber) {

        log.info("ENTER TO findUserByDocumentNumber:: " + documentNumber);

        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(urlFindByDocument.concat(documentNumber))
                        .build()
                )
                .retrieve()
                .onStatus(
                        HttpStatus.INTERNAL_SERVER_ERROR::equals,
                        response -> {
                            log.info("ERROR status in findUserByDocumentNumber from API: {} for URI: " + response.statusCode());
                            return response.bodyToMono(String.class).map(Exception::new);
                        })
                .bodyToMono(UserResponseDto.class)
                .retry(retry)
                .map(response -> mapper.convertValue(response, User.class))
                .onErrorResume(error -> {
                    log.info("ERROR RestConsumer in findUserByDocumentNumber " + error.getMessage());
                    return Mono.error(new FundException(FundErrorEnum.DOCUMENT_IDENTIFICATION_NOT_FOUND_IN_AUTH));
                });
    }

    @Override
    public Flux<User> findUsersByEmail(List<String> emails) {

        log.info("ENTER TO findUsersByEmail:: " + emails);

        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(urlFindByEmails)
                        .queryParam("emails", String.join(",", emails))
                        .build()
                )
                .retrieve()
                .onStatus(
                        HttpStatus.INTERNAL_SERVER_ERROR::equals,
                        response -> {
                            log.info("ERROR  in findUsersByEmail from API: {} for URI: " + response.statusCode());
                            return response.bodyToMono(String.class).map(Exception::new);
                        })
                .bodyToFlux(UserResponseDto.class)
                .retry(retry)
                .map(response -> mapper.convertValue(response, User.class))
                .onErrorResume(error -> {
                    log.info("ERROR RestConsumer in findUsersByEmail :: " + error);
                    return Mono.error(new FundException(FundErrorEnum.EMAIL_NOT_FOUND));
                });

    }

    //TODO hacen falta los fallbacks
}
