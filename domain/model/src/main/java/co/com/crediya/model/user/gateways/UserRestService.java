package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRestService {

    Mono<User> findUserByDocumentNumber(String documentNumber);

    Flux<User> findUsersByEmail(List<String> emails);
}
