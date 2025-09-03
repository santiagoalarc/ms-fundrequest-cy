package co.com.crediya.usecase.handler;

import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRestService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class FundApplicationListUseCase {

    private final FundApplicationRepository fundApplicationRepository;
    private final UserRestService userRestService;
    private final LoanTypeRepository loanTypeRepository;


    private final Logger log = Logger.getLogger(FundApplicationListUseCase.class.getName());
    public Mono<PagedResult<FundAppCustomer>> findFundApplicationList(FundApplicationFilter filter, PageRequestModel pageRequestModel) {
        log.info("Finding fund applications with filters" + filter.toString());

        //fundStatus repository find by name and get the id for searching

        return fundApplicationRepository.findPagedByFilter(filter, pageRequestModel)
                .flatMap(this::mapFundApplicationsWithLoanType)
                .flatMap(this::mapFundApplicationsWithUserInfo)
                .doOnNext(result -> log.info("Found fund applications" + result.getTotalElements()))
                .doOnError(error -> log.info("Error finding fund applications" + error));
    }

    private Mono<PagedResult<FundAppCustomer>> mapFundApplicationsWithLoanType(PagedResult<FundAppCustomer> fundAppPage) {
        if (fundAppPage.getContent().isEmpty()) {
            return Mono.just(fundAppPage);
        }
        return loanTypeRepository.findAll()
                .collectMap(LoanType::getId)
                .flatMap(loanTypeMap -> Flux.fromIterable(fundAppPage.getContent())
                        .map(fundApplication -> {
                            LoanType loanType = loanTypeMap.get(fundApplication.getIdLoanType());
                            if (loanType != null) {
                                fundApplication.setTypeLoanName(loanType.getName());
                                fundApplication.setInterestRateTaa(loanType.getInterestRateTaa());
                            }
                            return fundApplication;
                        })
                        .collectList()
                        .map(enrichedList -> fundAppPage.toBuilder().content(enrichedList).build()));
    }

    private Mono<PagedResult<FundAppCustomer>> mapFundApplicationsWithUserInfo(PagedResult<FundAppCustomer> fundAppPage) {
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

}