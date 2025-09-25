package co.com.crediya.r2dbc.fundapplication;

import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class FundRepositoryAdapter extends ReactiveAdapterOperations<
        FundApplication,
        FundEntity,
        UUID,
        FundReactiveRepository
        > implements FundApplicationRepository {

    private final TransactionalOperator transactionalOperator;

    public FundRepositoryAdapter(FundReactiveRepository repository,
                                 ObjectMapper mapper,
                                 TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, FundApplication.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<FundApplication> save(FundApplication fundApplication) {
        return super.save(fundApplication)
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<PagedResult<FundAppCustomer>> findPagedByFilter(FundApplicationFilter filter, PageRequestModel pageRequest) {
        Pageable pageable = PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize());
        return repository.findAllByEmailAndStatusAndLoanType(filter.getEmail(), filter.getStatus(), filter.getLoanType(), pageable)
                .collectList()
                .zipWith(this.repository.countByFilters(filter.getEmail(), filter.getStatusId(), filter.getLoanTypeId()))
                .map(tupleFund -> PagedResult.<FundAppCustomer>builder()
                        .content(tupleFund.getT1().stream()
                                .map(fundEntity -> mapper.map(fundEntity, FundAppCustomer.class))
                                .collect(Collectors.toList()))
                        .totalElements(tupleFund.getT2())
                        .totalPages((int) Math.ceil((double) tupleFund.getT2() / pageRequest.getSize()))
                        .page(pageRequest.getPage())
                        .size(pageRequest.getSize())
                        .build());
    }

    @Override
    public Flux<FundApplication> findByEmail(String email) {
        return repository.findAllByEmail(email)
                .map(this::toEntity);
    }

    @Override
    public Flux<FundApplication> findByUpdateDateBetween(long startDate, long endDate) {

        return repository.findByUpdateDateBetween(startDate, endDate)
                .map(this::toEntity);
    }

}