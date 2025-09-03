package co.com.crediya.usecase.command.fundapplication;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.enums.FundStatusEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.user.gateways.UserRestService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class FundApplicationUseCase {

    private final FundApplicationRepository fundApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final UserRestService userRestService;
    private final Logger log = Logger.getLogger(FundApplicationUseCase.class.getName());

    public Mono<FundApplication> saveFundApplication(FundApplication fundApplication, String email){

        log.log(Level.INFO,"ENTER TO CREATE FUND APPLICATION - {}", fundApplication);

        return Mono.justOrEmpty(fundApplication)
                .filter(fundApp -> email.equals(fundApp.getEmail()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.TOKEN_USER_MISMATCH))))
                .flatMap(fundApp -> loanTypeRepository.findByName(fundApp.getLoanType())
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.LOAN_TYPE_ID_NOT_FOUND))))
                        .filter(loanType -> loanType.getMaxAmount().compareTo(fundApp.getAmount()) >= 0
                                && loanType.getMinAmount().compareTo(fundApp.getAmount()) <= 0)
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.LOAN_AMOUNT_OUT_OF_RANGE))))
                        .map(loanType -> fundApp.toBuilder()
                                .idLoanType(loanType.getId())
                                .build())
                )
                .flatMap(fundApp -> Mono.just(fundApp)
                        .flatMap(fund -> userRestService.findUserByDocumentNumber(fund.getDocumentIdentification()))
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.DOCUMENT_IDENTIFICATION_NOT_FOUND))))
                        .thenReturn(fundApp)
                )
                .map(fundReq -> fundReq.toBuilder()
                        .statusId(FundStatusEnum.PENDING.getId())
                        .build())
                .flatMap(fundApplicationRepository::save)
                .doOnError(err -> log.info("ERROR IN CREATE FUND APPLICATION " + err.getMessage()))
                .doOnSuccess(userCreated -> log.info("CREATE FUND APPLICATION SUCCESSFUL :: " + userCreated));
    }
}
