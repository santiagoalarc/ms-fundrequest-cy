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

@Service
@RequiredArgsConstructor
public class RestConsumer implements UserRestService {

    @Qualifier("webClientUser")
    private final WebClient client;

    private final ObjectMapper mapper;

    @Value("${adapter.restconsumer.user.url}")
    private String urlFindByDocument;

    @Value("${adapter.restconsumer.retry}")
    private Integer retry;


    @Override
    public Mono<User> findUserByDocumentNumber(String documentNumber) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(urlFindByDocument.concat(documentNumber))
                        .build()
                )
                .retrieve()
                .onStatus(
                        HttpStatus.INTERNAL_SERVER_ERROR::equals,
                        response -> response.bodyToMono(String.class).map(Exception::new))
                .bodyToMono(UserResponseDto.class)
                .retry(retry)
                .map(response -> mapper.convertValue(response, User.class))
                .onErrorResume(error -> Mono.error(new FundException(FundErrorEnum.DOCUMENT_IDENTIFICATION_NOT_FOUND)));
    }

    @Override
    public Flux<User> findUsersByEmail(List<String> emails) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("emails", emails) //TODO validar como se está enviando
                        .build()
                )
                .retrieve()
                .onStatus(
                        HttpStatus.INTERNAL_SERVER_ERROR::equals,
                        response -> response.bodyToMono(String.class).map(Exception::new))
                .bodyToFlux(UserResponseDto.class)//TODO ojo tal vez sea un mono en vez de un flux
                .retry(retry)
                .map(response -> mapper.convertValue(response, User.class))
                .onErrorResume(error -> Mono.error(new FundException(FundErrorEnum.DOCUMENT_IDENTIFICATION_NOT_FOUND))); //TODO modificar el error

    }
}
