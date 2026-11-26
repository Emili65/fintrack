package com.fintrack.budget.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetRequest(
        @NotNull(message = "La categoría es obligatoria")
        Long categoryId,

        @NotNull(message = "El año es obligatorio")
        @Min(value = 2000, message = "Año no válido")
        @Max(value = 2100, message = "Año no válido")
        Integer year,

        @NotNull(message = "El mes es obligatorio")
        @Min(value = 1, message = "El mes debe estar entre 1 y 12")
        @Max(value = 12, message = "El mes debe estar entre 1 y 12")
        Integer month,

        @NotNull(message = "El límite es obligatorio")
        @DecimalMin(value = "0.01", message = "El límite debe ser mayor que cero")
        @Digits(integer = 12, fraction = 2)
        BigDecimal limit
) {
}
