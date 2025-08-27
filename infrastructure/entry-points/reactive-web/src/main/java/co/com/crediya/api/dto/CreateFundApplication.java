package co.com.crediya.api.dto;

import java.math.BigDecimal;

public record CreateFundApplication(
        String id,
        String documentIdentification,
        BigDecimal amount,
        Long term,
        String email,
        String idLoanType) {
}
