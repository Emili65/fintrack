package com.fintrack.report;

import com.fintrack.category.Category;
import com.fintrack.category.CategoryType;
import com.fintrack.report.dto.CategoryBreakdown;
import com.fintrack.report.dto.CategoryTotal;
import com.fintrack.report.dto.MonthlyPoint;
import com.fintrack.report.dto.SummaryResponse;
import com.fintrack.transaction.Transaction;
import com.fintrack.transaction.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 1, 31);

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private ReportService service;

    @Test
    void summaryCalculatesBalanceAndSavingsRate() {
        when(repository.sumByType(1L, CategoryType.INCOME, FROM, TO)).thenReturn(new BigDecimal("4000000"));
        when(repository.sumByType(1L, CategoryType.EXPENSE, FROM, TO)).thenReturn(new BigDecimal("3000000"));
        when(repository.countInPeriod(1L, FROM, TO)).thenReturn(12L);

        SummaryResponse summary = service.summary(1L, FROM, TO);

        assertThat(summary.balance()).isEqualByComparingTo("1000000");
        assertThat(summary.savingsRate()).isEqualByComparingTo("25.0");
        assertThat(summary.transactionCount()).isEqualTo(12);
    }

    @Test
    void summaryWithoutIncomeHasZeroSavingsRate() {
        when(repository.sumByType(eq(1L), any(), eq(FROM), eq(TO))).thenReturn(BigDecimal.ZERO);

        assertThat(service.summary(1L, FROM, TO).savingsRate()).isEqualByComparingTo("0");
    }

    @Test
    void byCategoryComputesPercentages() {
        when(repository.totalsByCategory(1L, CategoryType.EXPENSE, FROM, TO)).thenReturn(List.of(
                new CategoryTotal(1L, "Vivienda", "#000000", new BigDecimal("750")),
                new CategoryTotal(2L, "Comida", "#FFFFFF", new BigDecimal("250"))));

        List<CategoryBreakdown> result = service.byCategory(1L, CategoryType.EXPENSE, FROM, TO);

        assertThat(result).extracting(CategoryBreakdown::percentage)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("75.0"), new BigDecimal("25.0"));
    }

    @Test
    void monthlyAggregationAlwaysReturnsTwelveMonths() {
        Category salary = new Category(null, "Salario", CategoryType.INCOME, "#000000", null);
        Category food = new Category(null, "Comida", CategoryType.EXPENSE, "#000000", null);
        List<Transaction> transactions = List.of(
                new Transaction(null, salary, new BigDecimal("1000"), LocalDate.of(2026, 3, 1), null),
                new Transaction(null, food, new BigDecimal("300"), LocalDate.of(2026, 3, 9), null),
                new Transaction(null, food, new BigDecimal("200"), LocalDate.of(2026, 3, 20), null),
                new Transaction(null, food, new BigDecimal("80"), LocalDate.of(2026, 7, 2), null));

        List<MonthlyPoint> points = ReportService.aggregateByMonth(transactions);

        assertThat(points).hasSize(12);
        MonthlyPoint march = points.get(2);
        assertThat(march.income()).isEqualByComparingTo("1000");
        assertThat(march.expense()).isEqualByComparingTo("500");
        assertThat(march.balance()).isEqualByComparingTo("500");
        assertThat(points.get(6).balance()).isEqualByComparingTo("-80");
        assertThat(points.get(0).income()).isEqualByComparingTo("0");
    }
}
