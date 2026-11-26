package com.fintrack.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetLimitRequest(
        @NotNull(message = "El límite es obligatorio")
        @DecimalMin(value = "0.01", message = "El límite debe ser mayor que cero")
        @Digits(integer = 12, fraction = 2)
        BigDecimal limit
) {
}
