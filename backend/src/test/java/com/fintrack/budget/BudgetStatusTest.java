package com.fintrack.budget;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetStatusTest {

    @ParameterizedTest
    @CsvSource({
            "0, OK",
            "79.9, OK",
            "80, WARNING",
            "100, WARNING",
            "100.1, EXCEEDED",
            "250, EXCEEDED"
    })
    void statusDependsOnPercentageUsed(String percentage, BudgetStatus expected) {
        assertThat(BudgetStatus.fromPercentage(new BigDecimal(percentage))).isEqualTo(expected);
    }
}
