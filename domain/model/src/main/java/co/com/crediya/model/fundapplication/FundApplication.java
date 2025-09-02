package co.com.crediya.model.fundapplication;
import lombok.*;

import java.math.BigDecimal;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class FundApplication {

    private String documentIdentification;
    private BigDecimal amount;
    private Long term;
    private String email;
    private String statusId;
    private String idLoanType;
}
