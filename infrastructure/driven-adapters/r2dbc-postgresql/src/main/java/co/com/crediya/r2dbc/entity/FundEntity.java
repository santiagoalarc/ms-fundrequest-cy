package co.com.crediya.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("fund_application")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FundEntity {

    @Id
    private String id;
    private BigDecimal amount;
    private Long term;
    private String email;
    private String idStatus;
    private String idLoanType;
}
