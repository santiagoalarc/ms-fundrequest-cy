package co.com.crediya.usecase.handler;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import co.com.crediya.model.fundapplication.QueryFundApplication;
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

    private final Logger log = Logger.getLogger(FundApplicationListUseCase.class.getName());

    public Mono<PagedResult<FundAppCustomer>> findFundApplicationList(FundApplicationFilter fundApFilter, PageRequestModel pageRequestModel) {
        log.info("Finding fund applications with filters" + fundApFilter.toString());

        return Mono.just(fundApFilter)
                .flatMap(fundApplicationFilter -> fundApplicationRepository.findPagedByFilter(fundApplicationFilter, pageRequestModel)
                        .flatMap(this::mapFundWithStatusAndLoanTypeAndUser)
                        .flatMap(this::mapFundWithTotalAmountApproved)
                )
                .doOnNext(result -> log.info("Found fund applications" + result.getTotalElements()))
                .doOnError(error -> log.info("Error finding fund applications" + error));
    }

    private Mono<PagedResult<FundAppCustomer>> mapFundWithStatusAndLoanTypeAndUser(PagedResult<FundAppCustomer> fundAppPage) {

        QueryFundApplication queryFundApplication = QueryFundApplication.builder().build();

        List<String> emails = fundAppPage.getContent().stream()
                .map(FundApplication::getEmail)
                .collect(Collectors.toList());

        return Mono.just(fundAppPage)
                .filter(pagedFundApp -> !fundAppPage.getContent().isEmpty())
                .flatMap(pagedFundApp -> buildQueryFundApp(queryFundApplication, emails))
                .flatMap(queryFundApp -> Flux.fromIterable(fundAppPage.getContent())
                        .map(fundAppCustomer -> Optional.ofNullable(queryFundApp.getFundStatusMap()
                                        .get(fundAppCustomer.getIdStatus()))
                                .map(fundStatus -> {
                                    fundAppCustomer.setStatus(fundStatus.getName());
                                    return fundAppCustomer;
                                })
                                .orElse(fundAppCustomer))
                        .map(fundAppCustomer -> Optional.ofNullable(queryFundApp.getLoanTypeMap()
                                        .get(fundAppCustomer.getIdLoanType()))
                                .map(loanType -> {
                                    fundAppCustomer.setLoanType(loanType.getName());
                                    fundAppCustomer.setInterestRateTaa(loanType.getInterestRateTaa());
                                    return fundAppCustomer;
                                })
                                .orElse(fundAppCustomer))
                        .map(fundAppCustomer -> Optional.ofNullable(queryFundApp.getUserMap()
                                        .get(fundAppCustomer.getEmail()))
                                .map(user -> {
                                    fundAppCustomer.setName(user.getName().concat(" ").concat(user.getLastName()));
                                    fundAppCustomer.setEmail(user.getEmail());
                                    fundAppCustomer.setBaseSalary(user.getBaseSalary());
                                    return fundAppCustomer;
                                })
                                .orElse(fundAppCustomer)
                        )
                        .collectList()
                        .map(enrichedList -> fundAppPage.toBuilder().content(enrichedList).build())

                )
                .switchIfEmpty(Mono.defer(() -> Mono.just(fundAppPage)));
    }

    private Mono<QueryFundApplication> buildQueryFundApp(QueryFundApplication queryFundApplication, List<String> emails) {
        return fundStatusRepository.findAll()
                .collectMap(FundStatus::getId)
                .map(fundStatusMap -> queryFundApplication.toBuilder()
                        .fundStatusMap(fundStatusMap)
                        .build())
                .flatMap(queryFundApp -> loanTypeRepository.findAll()
                        .collectMap(LoanType::getId)
                        .map(loanTypeMap -> queryFundApp.toBuilder().loanTypeMap(loanTypeMap).build()))
                .flatMap(queryFundApp -> userRestService.findUsersByEmail(emails)
                        .distinct()
                        .collectMap(User::getEmail)
                        .map(userMap -> queryFundApp.toBuilder().userMap(userMap).build())
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