package co.com.crediya.model.events.gateway;

import co.com.crediya.model.fundapplication.FundApplication;
import reactor.core.publisher.Mono;

public interface EventGateway {
    Mono<Void> sendMessage(FundApplication fundApplication);
}
