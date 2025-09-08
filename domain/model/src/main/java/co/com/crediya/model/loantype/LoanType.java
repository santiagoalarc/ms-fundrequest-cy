package co.com.crediya.model.loantype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {

    private String id;
    private String name;
    private String creationDate;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;
    private Double interestRateTaa;
    private boolean autoValidation;
}
