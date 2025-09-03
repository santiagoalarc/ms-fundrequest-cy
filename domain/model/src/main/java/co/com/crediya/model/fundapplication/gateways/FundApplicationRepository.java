package co.com.crediya.model.fundapplication.gateways;

import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import reactor.core.publisher.Mono;

public interface FundApplicationRepository {

    Mono<FundApplication> save(FundApplication fundApplication);

    Mono<PagedResult<FundAppCustomer>> findPagedByFilter(FundApplicationFilter filter, PageRequestModel pageRequestModel);

}
