package co.com.crediya.usecase.handler;

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
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


@RequiredArgsConstructor
public class FundApplicationListUseCase {

    private final FundApplicationRepository fundApplicationRepository;
    private final FundStatusRepository fundStatusRepository;
    private final UserRestService userRestService;
    private final LoanTypeRepository loanTypeRepository;

    private final Logger log = Logger.getLogger(FundApplicationListUseCase.class.getName());

    public Mono<PagedResult<FundAppCustomer>> findFundApplicationList(FundApplicationFilter fundApFilter, PageRequestModel pageRequestModel) {
        log.info("Enter to FundApplicationListUseCase ::  with filters" + fundApFilter.toString());

        return Mono.just(fundApFilter)
                .flatMap(fundApplicationFilter -> fundApplicationRepository.findPagedByFilter(fundApplicationFilter, pageRequestModel))
                .flatMap(this::buildQueryFundApp)
                .flatMap(this::mapFundWithStatusAndLoanTypeAndUser)
                .doOnNext(result -> log.info("Found fund applications" + result.getTotalElements()))
                .doOnError(error -> log.info("Error finding fund applications" + error));
    }

    private Mono<PagedResult<FundAppCustomer>> mapFundWithStatusAndLoanTypeAndUser(QueryFundApplication queryFundApp) {

        return Mono.just(queryFundApp.getFundAppCustomerPaged())
                .filter(pagedFundApp -> !pagedFundApp.getContent().isEmpty())
                .map(PagedResult::getContent)
                .flatMapIterable(fundList -> fundList)
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
                            fundAppCustomer.setMonthlyAmount(fundAppCustomer.calculateMonthlyAmount(loanType.getInterestRateTaa()));
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
                .map(enrichedList -> queryFundApp.getFundAppCustomerPaged().toBuilder()
                        .content(enrichedList)
                        .build())

                .switchIfEmpty(Mono.defer(() -> Mono.just(queryFundApp.getFundAppCustomerPaged())));
    }

    private Mono<QueryFundApplication> buildQueryFundApp(PagedResult<FundAppCustomer> fundAppPage) {

        List<String> emails = fundAppPage.getContent().stream()
                .map(FundApplication::getEmail)
                .toList();

        return fundStatusRepository.findAll()
                .collectMap(FundStatus::getId)
                .map(fundStatusMap -> QueryFundApplication.builder()
                        .fundAppCustomerPaged(fundAppPage)
                        .emails(emails)
                        .fundStatusMap(fundStatusMap)
                        .build())
                .flatMap(queryFundApp -> loanTypeRepository.findAll()
                        .collectMap(LoanType::getId)
                        .map(loanTypeMap -> queryFundApp.toBuilder().loanTypeMap(loanTypeMap).build()))
                .flatMap(queryFundApp -> userRestService.findUsersByEmail(emails)
                        .distinct()
                        .collectMap(User::getEmail)
                        .map(userMap -> queryFundApp.toBuilder().userMap(userMap).build()));
    }

}