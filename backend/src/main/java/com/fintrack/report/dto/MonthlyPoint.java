package com.fintrack.report.dto;

import java.math.BigDecimal;

public record MonthlyPoint(int month, BigDecimal income, BigDecimal expense, BigDecimal balance) {
}
