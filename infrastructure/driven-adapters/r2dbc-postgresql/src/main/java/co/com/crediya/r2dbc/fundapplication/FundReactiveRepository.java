package co.com.crediya.r2dbc.fundapplication;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

// TODO: This file is just an example, you should delete or modify it
public interface FundReactiveRepository extends ReactiveCrudRepository<FundEntity, String>, ReactiveQueryByExampleExecutor<FundEntity> {

}
