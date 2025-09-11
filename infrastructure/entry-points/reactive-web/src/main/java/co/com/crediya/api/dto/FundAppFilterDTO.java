package co.com.crediya.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FundAppFilterDTO(
        UUID id,
        String name,
        String baseSalary,
        Double interestRateTaa,
        BigDecimal monthlyAmount,
        String status,
        BigDecimal amount,
        Long term,
        String email,
        String loanType) {
}
