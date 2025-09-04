package co.com.crediya.usecase.handler;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.model.fundstatus.FundStatus;
import co.com.crediya.model.fundstatus.gateways.FundStatusRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRestService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class FundApplicationListUseCase {

    private final FundApplicationRepository fundApplicationRepository;
    private final FundStatusRepository fundStatusRepository;
    private final UserRestService userRestService;
    private final LoanTypeRepository loanTypeRepository;

    private final static String EMPTY = "";


    private final Logger log = Logger.getLogger(FundApplicationListUseCase.class.getName());

    public Mono<PagedResult<FundAppCustomer>> findFundApplicationList(FundApplicationFilter fundApFilter, PageRequestModel pageRequestModel) {
        log.info("Finding fund applications with filters" + fundApFilter.toString());

        return createFundAppFilterData(fundApFilter)
                .flatMap(fundApplicationFilter -> fundApplicationRepository.findPagedByFilter(fundApplicationFilter, pageRequestModel)
                        .flatMap(this::mapFundWithLoanType)
                        .flatMap(this::mapFundWithFundStatus)
                        .flatMap(this::mapFundWithUserInfo)
                        .flatMap(this::mapFundWithTotalAmountApproved)
                )
                .doOnNext(result -> log.info("Found fund applications" + result.getTotalElements()))
                .doOnError(error -> log.info("Error finding fund applications" + error));
    }

    private Mono<FundApplicationFilter> createFundAppFilterData(FundApplicationFilter fundApFilter) {
        return Mono.just(fundApFilter)
                .filter(fundApFilterData -> !EMPTY.equals(fundApFilter.getStatus()))
                .flatMap(fundApplicationFilter -> fundStatusRepository.findByName(fundApFilter.getStatus())
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.FUND_STATUS_INVALID)))))
                .map(fundStatus -> fundApFilter.toBuilder()
                        .statusId(fundStatus.getId().toString())
                        .build())
                .switchIfEmpty(Mono.defer(() -> Mono.just(fundApFilter)))
                .flatMap(fundApplicationFilter -> Mono.just(fundApplicationFilter)
                        .filter(fundApFilterData -> !EMPTY.equals(fundApFilter.getLoanType()))
                        .flatMap(fundApFilterData -> loanTypeRepository.findByName(fundApFilter.getLoanType())
                                .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.LOAN_TYPE_INVALID)))))
                        .map(loanType -> fundApplicationFilter.toBuilder()
                                .loanTypeId(loanType.getId().toString())
                                .build())
                        .switchIfEmpty(Mono.defer(() -> Mono.just(fundApplicationFilter)))
                );
    }

    private Mono<PagedResult<FundAppCustomer>> mapFundWithLoanType(PagedResult<FundAppCustomer> fundAppPage) {
        if (fundAppPage.getContent().isEmpty()) {
            return Mono.just(fundAppPage);
        }
        return loanTypeRepository.findAll()
                .collectMap(LoanType::getId)
                .flatMap(loanTypeMap -> Flux.fromIterable(fundAppPage.getContent())
                        .map(fundApplication -> {
                            LoanType loanType = loanTypeMap.get(fundApplication.getIdLoanType());
                            if (loanType != null) {
                                fundApplication.setLoanType(loanType.getName());
                                fundApplication.setInterestRateTaa(loanType.getInterestRateTaa());
                            }
                            return fundApplication;
                        })
                        .collectList()
                        .map(enrichedList -> fundAppPage.toBuilder().content(enrichedList).build())
                );
    }

    private Mono<PagedResult<FundAppCustomer>> mapFundWithUserInfo(PagedResult<FundAppCustomer> fundAppPage) {
        if (fundAppPage.getContent().isEmpty()) {
            return Mono.just(fundAppPage);
        }
        List<String> emails = fundAppPage.getContent().stream()
                .map(FundApplication::getEmail)
                .collect(Collectors.toList());

        return userRestService.findUsersByEmail(emails)
                .distinct()
                .collectMap(User::getEmail)
                .flatMap(userInfoMap -> Flux.fromIterable(fundAppPage.getContent())
                        .map(fundApp -> {
                            User user = userInfoMap.get(fundApp.getEmail());
                            if (user != null) {
                                fundApp.setName(user.getName().concat(" ").concat(user.getLastName()));
                                fundApp.setBaseSalary(user.getBaseSalary());
                            }
                            return fundApp;
                        })
                        .collectList()
                        .map(enrichedList -> fundAppPage.toBuilder().content(enrichedList).build()));
    }

    private Mono<PagedResult<FundAppCustomer>> mapFundWithFundStatus(PagedResult<FundAppCustomer> fundAppPage) {
        if (fundAppPage.getContent().isEmpty()) {
            return Mono.just(fundAppPage);
        }
        return fundStatusRepository.findAll()
                .collectMap(FundStatus::getId)
                .flatMap(fundStatusMap -> Flux.fromIterable(fundAppPage.getContent())
                        .map(fundApplication -> {
                            FundStatus fundStatus = fundStatusMap.get(fundApplication.getIdStatus());
                            if (fundStatus != null) {
                                fundApplication.setStatus(fundStatus.getName());
                            }
                            return fundApplication;
                        })
                        .collectList()
                        .map(enrichedList -> fundAppPage.toBuilder().content(enrichedList).build())
                );
    }

    private Mono<PagedResult<FundAppCustomer>> mapFundWithTotalAmountApproved(PagedResult<FundAppCustomer> fundAppPage) {

        if (fundAppPage.getContent().isEmpty()) {
            return Mono.just(fundAppPage);
        }
        List<String> emails = fundAppPage.getContent().stream()
                .map(FundApplication::getEmail)
                .toList();

        return fundStatusRepository.findByName("APPROVED")
                .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.STATUS_FUND_NAME_NOT_FOUND))))
                .map(FundStatus::getId)
                .flatMap(fundStatusId -> fundApplicationRepository.findAllByEmailIn(emails)
                        .filter(fundApplication -> fundStatusId.equals(fundApplication.getIdStatus()))
                        .groupBy(FundApplication::getEmail)
                        .flatMap(groupedFlux -> groupedFlux
                                .map(FundApplication::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .map(totalAmount -> Map.entry(groupedFlux.key(), totalAmount))
                        )
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                        .flatMap(mapResult -> Flux.fromIterable(fundAppPage.getContent())
                                .map(fundApp -> {
                                    BigDecimal amountApproved = mapResult.get(fundApp.getEmail());
                                    fundApp.setTotalDebt(Optional.ofNullable(amountApproved).orElse(BigDecimal.ZERO));
                                    return fundApp;
                                })
                                .collectList()
                                .map(enrichedList -> fundAppPage.toBuilder().content(enrichedList).build()))
                );

    }

}