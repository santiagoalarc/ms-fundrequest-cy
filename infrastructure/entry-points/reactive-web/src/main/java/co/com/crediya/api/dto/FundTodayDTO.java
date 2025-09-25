package co.com.crediya.api.dto;

import java.math.BigDecimal;

public record FundTodayDTO(
        BigDecimal amount,
        Long term
) {
}