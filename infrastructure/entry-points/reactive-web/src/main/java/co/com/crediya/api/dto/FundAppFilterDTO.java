package co.com.crediya.api.dto;

import java.math.BigDecimal;

public record FundAppFilterDTO(String name,
                               String baseSalary,
                               BigDecimal interestRateTaa,
                               BigDecimal totalDebt,
                               String status,
                               BigDecimal amount,
                               Long term,
                               String email,
                               String loanType) {
}
