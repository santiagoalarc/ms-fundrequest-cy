package co.com.crediya.model.fundapplication.gateways;

import co.com.crediya.model.fundapplication.FundApplication;
import reactor.core.publisher.Mono;

public interface FundApplicationRepository {

    Mono<FundApplication> save(FundApplication fundApplication);
}
