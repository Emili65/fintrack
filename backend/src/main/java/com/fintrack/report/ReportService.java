package com.fintrack.report;

import com.fintrack.category.CategoryType;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.report.dto.CategoryBreakdown;
import com.fintrack.report.dto.CategoryTotal;
import com.fintrack.report.dto.MonthlyPoint;
import com.fintrack.report.dto.SummaryResponse;
import com.fintrack.transaction.Transaction;
import com.fintrack.transaction.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary(Long userId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        BigDecimal income = transactionRepository.sumByType(userId, CategoryType.INCOME, from, to);
        BigDecimal expense = transactionRepository.sumByType(userId, CategoryType.EXPENSE, from, to);
        BigDecimal balance = income.subtract(expense);
        BigDecimal savingsRate = income.signum() == 0
                ? BigDecimal.ZERO
                : balance.multiply(HUNDRED).divide(income, 1, RoundingMode.HALF_UP);
        long count = transactionRepository.countInPeriod(userId, from, to);
        return new SummaryResponse(from, to, income, expense, balance, savingsRate, count);
    }

    @Transactional(readOnly = true)
    public List<CategoryBreakdown> byCategory(Long userId, CategoryType type, LocalDate from, LocalDate to) {
        validateRange(from, to);
        List<CategoryTotal> totals = transactionRepository.totalsByCategory(userId, type, from, to);
        BigDecimal grandTotal = totals.stream().map(CategoryTotal::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return totals.stream()
                .map(t -> new CategoryBreakdown(t.categoryId(), t.name(), t.color(), t.total(),
                        grandTotal.signum() == 0 ? BigDecimal.ZERO
                                : t.total().multiply(HUNDRED).divide(grandTotal, 1, RoundingMode.HALF_UP)))
                .toList();
    }

    /** Ingresos y gastos de cada mes del año (siempre devuelve los 12 meses). */
    @Transactional(readOnly = true)
    public List<MonthlyPoint> monthly(Long userId, int year) {
        List<Transaction> transactions = transactionRepository.findInPeriodWithCategory(
                userId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        return aggregateByMonth(transactions);
    }

    static List<MonthlyPoint> aggregateByMonth(List<Transaction> transactions) {
        BigDecimal[] income = filledWithZero();
        BigDecimal[] expense = filledWithZero();
        for (Transaction t : transactions) {
            int idx = t.getDate().getMonthValue() - 1;
            if (t.getCategory().getType() == CategoryType.INCOME) {
                income[idx] = income[idx].add(t.getAmount());
            } else {
                expense[idx] = expense[idx].add(t.getAmount());
            }
        }
        List<MonthlyPoint> points = new ArrayList<>(12);
        IntStream.range(0, 12).forEach(i ->
                points.add(new MonthlyPoint(i + 1, income[i], expense[i], income[i].subtract(expense[i]))));
        return points;
    }

    private static BigDecimal[] filledWithZero() {
        BigDecimal[] values = new BigDecimal[12];
        java.util.Arrays.fill(values, BigDecimal.ZERO);
        return values;
    }

    private static void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La fecha inicial no puede ser posterior a la final");
        }
    }
}
