package co.com.crediya.r2dbc.fundapplication;


import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
public class FundRepositoryAdapter extends ReactiveAdapterOperations<
        FundApplication,
        FundEntity,
        String,
        FundReactiveRepository
> implements FundApplicationRepository {

    private final TransactionalOperator transactionalOperator;
    public FundRepositoryAdapter(FundReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, FundApplication.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<FundApplication> save(FundApplication fundApplication) {
        return super.save(fundApplication)
                .as(transactionalOperator::transactional);
    }
}
