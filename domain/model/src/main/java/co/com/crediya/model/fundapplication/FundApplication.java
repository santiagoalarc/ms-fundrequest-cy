package co.com.crediya.model.fundapplication;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public BigDecimal calculateMonthlyAmount(Double interestRateTaa) {

        BigDecimal interestRateBigDecimal = BigDecimal.valueOf(interestRateTaa);
        BigDecimal interestPercentage = interestRateBigDecimal.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
        BigDecimal sameTerm = BigDecimal.ONE.add(interestPercentage).pow(this.term.intValue());
        BigDecimal numerator = this.amount.multiply(interestPercentage).multiply(sameTerm);
        BigDecimal denominator = sameTerm.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
    }
}


