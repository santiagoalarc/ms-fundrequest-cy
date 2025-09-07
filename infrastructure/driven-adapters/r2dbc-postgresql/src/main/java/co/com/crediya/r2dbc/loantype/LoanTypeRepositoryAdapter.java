package co.com.crediya.r2dbc.loantype;


import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public class LoanTypeRepositoryAdapter extends ReactiveAdapterOperations<
        LoanType,
        LoanTypeEntity,
        String,
        LoanTypeReactiveRepository>
implements LoanTypeRepository {

    public LoanTypeRepositoryAdapter(LoanTypeReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, LoanType.class));
    }

    @Override
    public Mono<LoanType> findByName(String name) {
        return repository.findByName(name)
                .map(this::toEntity);
    }

}
