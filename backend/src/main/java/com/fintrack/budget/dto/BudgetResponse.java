package com.fintrack.budget.dto;

import com.fintrack.budget.BudgetStatus;
import com.fintrack.category.dto.CategoryResponse;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        CategoryResponse category,
        int year,
        int month,
        BigDecimal limit,
        BigDecimal spent,
        BigDecimal remaining,
        BigDecimal percentUsed,
        BudgetStatus status
) {
}
