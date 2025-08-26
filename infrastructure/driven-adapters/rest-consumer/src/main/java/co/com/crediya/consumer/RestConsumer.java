package co.com.crediya.consumer;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

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

    // these methods are an example that illustrates the implementation of WebClient.
    // You should use the methods that you implement from the Gateway from the domain.
    @CircuitBreaker(name = "testGet" /*, fallbackMethod = "testGetOk"*/)
    public Mono<ObjectResponse> testGet() {
        return client
                .get()
                .retrieve()
                .bodyToMono(ObjectResponse.class);
    }

// Possible fallback method
//    public Mono<String> testGetOk(Exception ignored) {
//        return client
//                .get() // TODO: change for another endpoint or destination
//                .retrieve()
//                .bodyToMono(String.class);
//    }

    @CircuitBreaker(name = "testPost")
    public Mono<ObjectResponse> testPost() {
        ObjectRequest request = ObjectRequest.builder()
            .val1("exampleval1")
            .val2("exampleval2")
            .build();
        return client
                .post()
                .body(Mono.just(request), ObjectRequest.class)
                .retrieve()
                .bodyToMono(ObjectResponse.class);
    }

    @Override
    public Mono<User> findUserByDocumentNumber(String documentNumber) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(urlFindByDocument)
                        .queryParam("document_number", Objects.requireNonNullElse(documentNumber, ""))
                        .build()
                )
                .retrieve()
                .bodyToMono(UserRequestDto.class)
                //.timeout(Duration.ofMinutes(ONE_MINUTES))
                .retry(retry)
                .map(response -> mapper.convertValue(response, User.class));
                //.cache(Duration.ofMinutes(FIVE_MINUTES))
                //.doOnError(error -> log.info("ERROR IN ExperienceRestConsumer - findCustomizationByCorporateIdAndStatus - ERROR MESSAGE {}", error.getMessage()))
                //.onErrorResume(error -> Mono.error(new AdapterException(ExperienceExceptionEnum.ERROR_WHEN_OBTAINING_PERSONALIZATION_BY_CORPORATE_ID.name())))
        // ;
    }
}
