package co.com.crediya.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateFundApplication(
        @NotBlank(message = "NAME_CANNOT_BE_EMPTY_OR_NULL")
        String documentIdentification,
        BigDecimal amount,
        Long term,
        @NotBlank(message = "EMAIL_CANNOT_BE_EMPTY_OR_NULL")
        @Email(message = "EMAIL_IS_NOT_VALID")
        String email,
        String loanType) {
}
