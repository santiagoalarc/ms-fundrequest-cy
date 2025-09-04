package co.com.crediya.r2dbc.fundapplication;


import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface FundReactiveRepository extends ReactiveCrudRepository<FundEntity, String>, ReactiveQueryByExampleExecutor<FundEntity> {

    @Query("""
        SELECT * FROM fund_application
        WHERE (:email IS NULL OR COALESCE(:email, '') = '' OR email = :email)
        AND (:status IS NULL OR COALESCE(:status, '') = '' OR id_status = :status)
        AND (:loanType IS NULL OR COALESCE(:loanType, '') = '' OR id_loan_type = :loanType)
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
        """)
    Flux<FundEntity> findAllByEmailAndStatusAndLoanType(
            @Param("email") String email,
            @Param("status") String status,
            @Param("loanType") String loanType,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(*) FROM fund_application
    WHERE (:email IS NULL OR COALESCE(:email, '') = '' OR email = :email)
    AND (:status IS NULL OR COALESCE(:status, '') = '' OR id_status = :status)
    AND (:loanType IS NULL OR COALESCE(:loanType, '') = '' OR id_loan_type = :loanType)
    """)
    Mono<Long> countByFilters(
            @Param("email") String email,
            @Param("status") String status,
            @Param("loanType") String loanType
    );

    Flux<FundEntity> findAllByEmailIn(List<String> emails);

}