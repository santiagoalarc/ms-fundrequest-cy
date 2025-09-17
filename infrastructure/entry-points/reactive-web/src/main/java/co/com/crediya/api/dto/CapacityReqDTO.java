package co.com.crediya.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.UUID;


public record CapacityReqDTO(
        @NotBlank(message = "FUNDID_CANNOT_BE_EMPTY_OR_NULL")
        @UUID
        String fundId,
        @NotBlank(message = "EMAIL_CANNOT_BE_EMPTY_OR_NULL")
        String email
) {
}
