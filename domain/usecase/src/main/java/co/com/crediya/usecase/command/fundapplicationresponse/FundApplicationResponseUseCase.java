package co.com.crediya.usecase.command.fundapplicationresponse;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.enums.FundStatusEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationNotificationGateway;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.model.user.gateways.UserRestService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class FundApplicationResponseUseCase {

    private final FundApplicationRepository fundApplicationRepository;
    private final FundApplicationNotificationGateway notificationGateway;
    private final UserRestService userRestService;

    private final Logger log = Logger.getLogger(FundApplicationResponseUseCase.class.getName());

    public Mono<FundApplication> execute(FundApplication fundApplication) {

        log.info("ENTER TO FundApplicationResponseUseCase " + fundApplication);

        return Mono.just(fundApplication.getStatus())
                .map(FundStatusEnum::getIdFromName)
                .onErrorResume(throwable -> Mono.error(new FundException(FundErrorEnum.OBJECT_STATUS_ID_NOT_VALID)))
                .flatMap(statusId -> fundApplicationRepository.findById(fundApplication.getId())
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.FUND_APPLICATION_ID_NOT_FOUND))))
                        .map(fundReq -> fundReq.toBuilder()
                                .idStatus(statusId)
                                .build()))
                .flatMap(fundApplicationRepository::save)
                .flatMap(fundData -> Mono.just(fundData)
                        .flatMap(fundDataInfo -> userRestService.findUsersByEmail(List.of(fundDataInfo.getEmail())).next())
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.EMAIL_NOT_FOUND))))
                        .map(user -> user.getName().concat(" ").concat(user.getLastName()))
                        .flatMap(userName -> notificationGateway.notifyStatusChange(fundData.getEmail(),
                                "ACTUALIZACIÓN DE ESTADO DE SOLICITUD DE PRÉSTAMO",
                                userName,
                                fundApplication.getStatus()))
                )
                .thenReturn(fundApplication)
                .doOnSuccess(fundUpdated -> log.info("UPDATE FUND APPLICATION SUCCESSFUL :: " + fundUpdated));
    }
}
