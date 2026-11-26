package com.fintrack.transaction.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "La categoría es obligatoria")
        Long categoryId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
        @Digits(integer = 12, fraction = 2, message = "Máximo 12 enteros y 2 decimales")
        BigDecimal amount,

        @NotNull(message = "La fecha es obligatoria")
        @PastOrPresent(message = "La fecha no puede estar en el futuro")
        LocalDate date,

        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String description
) {
}
