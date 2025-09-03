package co.com.crediya.model.loantype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {

    private UUID id;
    private String name;
    private String creationDate;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;
    private BigDecimal interestRateTaa;
    private boolean autoValidation;
}
