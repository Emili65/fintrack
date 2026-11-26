package com.fintrack.transaction.dto;

import com.fintrack.category.dto.CategoryResponse;
import com.fintrack.transaction.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String description,
        CategoryResponse category
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAmount(), t.getDate(), t.getDescription(),
                CategoryResponse.from(t.getCategory()));
    }
}
