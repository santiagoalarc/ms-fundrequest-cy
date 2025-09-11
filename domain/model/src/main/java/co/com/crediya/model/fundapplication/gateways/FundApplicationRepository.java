package co.com.crediya.model.fundapplication.gateways;

import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FundApplicationRepository {

    Mono<FundApplication> findById(UUID id);

    Mono<FundApplication> save(FundApplication fundApplication);

    Mono<PagedResult<FundAppCustomer>> findPagedByFilter(FundApplicationFilter filter, PageRequestModel pageRequestModel);

    Flux<FundApplication> findByEmail(String email);

}
