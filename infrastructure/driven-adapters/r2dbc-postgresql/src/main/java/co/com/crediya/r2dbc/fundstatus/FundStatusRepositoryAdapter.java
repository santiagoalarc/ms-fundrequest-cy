package co.com.crediya.r2dbc.fundstatus;

import co.com.crediya.model.fundstatus.FundStatus;
import co.com.crediya.model.fundstatus.gateways.FundStatusRepository;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class FundStatusRepositoryAdapter  extends ReactiveAdapterOperations<
        FundStatus,
        FundStatusEntity,
        String,
        FundStatusReactiveRepository
        > implements FundStatusRepository {

    protected FundStatusRepositoryAdapter(FundStatusReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, FundStatus.class));
    }

    @Override
    public Mono<FundStatus> findByName(String name) {
        return repository.findByName(name)
                .map(this::toEntity);
    }
}
