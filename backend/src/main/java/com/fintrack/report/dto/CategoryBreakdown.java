package com.fintrack.report.dto;

import java.math.BigDecimal;

public record CategoryBreakdown(Long categoryId, String name, String color, BigDecimal total, BigDecimal percentage) {
}
