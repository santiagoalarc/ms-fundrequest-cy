package co.com.crediya.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.UUID;


public record UpdateFundDTO(
        @NotBlank(message = "ID_CANNOT_BE_EMPTY_OR_NULL")
        @UUID
        String id,
        @NotBlank(message = "STATUS_CANNOT_BE_EMPTY_OR_NULL")
        String status
) {
}
