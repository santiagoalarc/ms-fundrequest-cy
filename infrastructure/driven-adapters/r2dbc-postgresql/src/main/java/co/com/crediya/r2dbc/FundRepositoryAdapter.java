package co.com.crediya.r2dbc;


import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.r2dbc.entity.FundEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class FundRepositoryAdapter extends ReactiveAdapterOperations<
        FundApplication,
        FundEntity,
        String,
        FundReactiveRepository
> implements FundApplicationRepository {
    public FundRepositoryAdapter(FundReactiveRepository repository, ObjectMapper mapper) {

        super(repository, mapper, entity -> mapper.map(entity, FundApplication.class));
    }

    @Override
    public Mono<FundApplication> save(FundApplication fundApplication) {
        return Mono.justOrEmpty(fundApplication)
                .map(this::toData)
                .flatMap(fun -> repository.save(fun))
                .map(this::toEntity);
    }
}
