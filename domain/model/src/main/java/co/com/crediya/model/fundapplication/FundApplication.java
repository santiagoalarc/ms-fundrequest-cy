package co.com.crediya.model.fundapplication;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class FundApplication {

    private UUID id;
    private String documentIdentification;
    private BigDecimal amount;
    private Long term;
    private String email;
    private String idStatus;
    private String idLoanType;
    private String loanType;
    private String status;
}
