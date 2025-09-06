package co.com.crediya.r2dbc.loantype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("loan_type")
public class LoanTypeEntity {

    @Id
    private String id;
    private String name;
    @Column("creation_date")
    private Long creationDate;
    @Column("max_amount")
    private BigDecimal maxAmount;
    @Column("min_amount")
    private BigDecimal minAmount;
    @Column("interest_rate_taa")
    private BigDecimal interestRateTaa;
    @Column("auto_validation")
    private Boolean autoValidation;
}