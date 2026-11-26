package com.fintrack.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SummaryResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        /** Porcentaje del ingreso que se ahorró en el periodo. */
        BigDecimal savingsRate,
        long transactionCount
) {
}
