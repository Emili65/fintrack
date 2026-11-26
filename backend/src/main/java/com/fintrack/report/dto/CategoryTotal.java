package com.fintrack.report.dto;

import java.math.BigDecimal;

/** Proyección usada por la consulta JPQL de totales agrupados por categoría. */
public record CategoryTotal(Long categoryId, String name, String color, BigDecimal total) {
}
