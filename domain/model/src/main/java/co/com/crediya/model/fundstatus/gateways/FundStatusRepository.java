package co.com.crediya.model.fundstatus.gateways;

import co.com.crediya.model.fundstatus.FundStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FundStatusRepository {

    Mono<FundStatus> findByName(String name);

    Flux<FundStatus> findAll();
}
