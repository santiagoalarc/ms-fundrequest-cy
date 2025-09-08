package co.com.crediya.r2dbc.fundapplication;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("fund_application")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FundEntity {

    @Id
    private UUID id;
    private BigDecimal amount;
    private Long term;
    private String email;
    @Column("id_status")
    private String idStatus;
    @Column("id_loan_type")
    private String idLoanType;
}
