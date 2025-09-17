package co.com.crediya.usecase.common;

import co.com.crediya.enums.FundStatusEnum;
import co.com.crediya.model.events.gateway.EventGateway;
import co.com.crediya.model.fundapplication.FundApplication;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ValidateApprovalStatusUseCase {

    private final EventGateway eventGateway;


    public Mono<Void> validateStatusAndSend(FundApplication fundApplication) {

        return Mono.just(fundApplication)
                .filter(loanApp -> FundStatusEnum.APPROVED.getId().equals(fundApplication.getIdStatus()))
                .flatMap(eventGateway::sendMessage)
                .switchIfEmpty(Mono.defer(Mono::empty))
                .thenEmpty(Mono.empty());
    }
}
