package com.fintrack.transaction;

import com.fintrack.category.CategoryType;

import java.time.LocalDate;

/** Filtros opcionales para el listado de transacciones. */
public record TransactionFilter(
        LocalDate from,
        LocalDate to,
        Long categoryId,
        CategoryType type,
        String search
) {
}
