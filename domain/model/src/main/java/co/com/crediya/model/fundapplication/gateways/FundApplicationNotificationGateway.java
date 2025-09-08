package co.com.crediya.model.fundapplication.gateways;

import reactor.core.publisher.Mono;

public interface FundApplicationNotificationGateway {

    Mono<String> notifyStatusChange(String email, String subject, String userName, String status);

}
