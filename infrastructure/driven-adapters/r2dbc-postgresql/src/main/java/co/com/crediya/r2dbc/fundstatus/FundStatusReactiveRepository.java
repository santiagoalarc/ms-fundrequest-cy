package co.com.crediya.r2dbc.fundstatus;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface FundStatusReactiveRepository  extends ReactiveCrudRepository<FundStatusEntity, String>, ReactiveQueryByExampleExecutor<FundStatusEntity> {

    Mono<FundStatusEntity> findByName(String name);
}
