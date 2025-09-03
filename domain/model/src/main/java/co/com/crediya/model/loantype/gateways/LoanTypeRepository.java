package co.com.crediya.model.loantype.gateways;

import co.com.crediya.model.loantype.LoanType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {

    Mono<LoanType> findByName(String name);

    Flux<LoanType> findAll();
}
