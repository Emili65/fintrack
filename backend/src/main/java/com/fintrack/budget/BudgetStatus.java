package com.fintrack.budget;

import java.math.BigDecimal;

public enum BudgetStatus {
    /** Menos del 80 % del límite consumido. */
    OK,
    /** Entre el 80 % y el 100 %. */
    WARNING,
    /** Límite superado. */
    EXCEEDED;

    private static final BigDecimal WARNING_THRESHOLD = BigDecimal.valueOf(80);
    private static final BigDecimal LIMIT = BigDecimal.valueOf(100);

    public static BudgetStatus fromPercentage(BigDecimal percentage) {
        if (percentage.compareTo(LIMIT) > 0) {
            return EXCEEDED;
        }
        if (percentage.compareTo(WARNING_THRESHOLD) >= 0) {
            return WARNING;
        }
        return OK;
    }
}
