package co.com.crediya.usecase.handler;

import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.user.gateways.UserRestService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;


@RequiredArgsConstructor
public class FundApplicationListUseCase {

    private final FundApplicationRepository fundApplicationRepository;
    private final UserRestService userRestService;
    private final LoanTypeRepository loanTypeRepository;


    private final Logger log = Logger.getLogger(FundApplicationListUseCase.class.getName());
    public Mono<PagedResult<FundAppCustomer>> findFundApplicationList(FundApplicationFilter filter, PageRequestModel pageRequestModel) {
        log.info("Finding fund applications with filters" + filter.toString());

        return fundApplicationRepository.findPagedByFilter(filter, pageRequestModel)
                .flatMap(this::mapLoanTypeName)
                /*.flatMap(fundAppCustomerPaged -> Flux.fromIterable(fundAppCustomerPaged.getContent())
                        .map(FundApplication::getEmail)
                        .collectList()
                        .flatMapMany(emails -> userRestService.findUsersByEmail(emails)
                                .distinct()
                                .collectMap(User::getEmail)

                        )
                )*/
                .doOnNext(result -> log.info("Found fund applications" + result.getTotalElements()))
                .doOnError(error -> log.info("Error finding fund applications" + error));
    }



    private Mono<PagedResult<FundAppCustomer>> mapLoanTypeName(PagedResult<FundAppCustomer> fundAppPage) {
        return loanTypeRepository.findAll()
                .collectMap(LoanType::getId)
                .flatMap(loanTypeMap -> Flux.fromIterable(fundAppPage.getContent())
                        .map(fundApplication -> {
                            fundApplication.setTypeLoanName(loanTypeMap.get(fundApplication.getIdLoanType()).getName());
                            fundApplication.setInterestRateTaa(loanTypeMap.get(fundApplication.getIdLoanType()).getInterestRateTaa());
                            return fundApplication;
                        })
                        .collectList()
                        .map(fundApplicationList -> fundAppPage
                                .toBuilder()
                                .content(fundApplicationList)
                                .build()));
    }


}