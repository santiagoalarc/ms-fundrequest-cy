package co.com.crediya.api.dto;

import java.math.BigDecimal;

public record FundAppFilterDTO(String name,
                               String baseSalary,
                               Double interestRateTaa,
                               BigDecimal monthlyAmount,
                               String status,
                               BigDecimal amount,
                               Long term,
                               String email,
                               String loanType) {
}
