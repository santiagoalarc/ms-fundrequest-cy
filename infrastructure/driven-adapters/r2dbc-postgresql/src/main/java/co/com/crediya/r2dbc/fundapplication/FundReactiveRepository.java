package co.com.crediya.r2dbc.fundapplication;


import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface FundReactiveRepository extends ReactiveCrudRepository<FundEntity, UUID>, ReactiveQueryByExampleExecutor<FundEntity> {

    @Query("""
       SELECT fa.* FROM fund_application fa
       INNER JOIN fund_status fs2 on fa.id_status = fs2.id
       INNER JOIN loan_type lt on lt.id = fa.id_loan_type
       WHERE (:email IS NULL OR COALESCE(:email, '') = '' OR email = :email)
       AND (:status IS NULL OR COALESCE(:status, '') = '' OR fs2."name"  = :status)
       AND (:loanType IS NULL OR COALESCE(:loanType, '') = '' OR lt."name"  = :loanType)
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
        """)
    Flux<FundEntity> findAllByEmailAndStatusAndLoanType(
            @Param("email") String email,
            @Param("status") String status,
            @Param("loanType") String loanType,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(fa.*) FROM fund_application fa
    INNER JOIN fund_status fs2 on fa.id_status = fs2.id
    INNER JOIN loan_type lt on lt.id = fa.id_loan_type
    WHERE (:email IS NULL OR COALESCE(:email, '') = '' OR email = :email)
    AND (:status IS NULL OR COALESCE(:status, '') = '' OR fs2."name"  = :status)
    AND (:loanType IS NULL OR COALESCE(:loanType, '') = '' OR lt."name"  = :loanType)
    """)
    Mono<Long> countByFilters(
            @Param("email") String email,
            @Param("status") String status,
            @Param("loanType") String loanType
    );

    Flux<FundEntity> findAllByEmail(String email);

    @Query("SELECT * FROM fund_application WHERE update_date BETWEEN :startDate AND :endDate")
    Flux<FundEntity> findByUpdateDateBetween(long startDate, long endDate);

}