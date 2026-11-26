package com.fintrack.budget;

import com.fintrack.budget.dto.BudgetLimitRequest;
import com.fintrack.budget.dto.BudgetRequest;
import com.fintrack.budget.dto.BudgetResponse;
import com.fintrack.category.Category;
import com.fintrack.category.CategoryService;
import com.fintrack.category.CategoryType;
import com.fintrack.category.dto.CategoryResponse;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.report.dto.CategoryTotal;
import com.fintrack.transaction.TransactionRepository;
import com.fintrack.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository, TransactionRepository transactionRepository,
                         CategoryService categoryService, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    /** Lista los presupuestos del mes con lo gastado hasta ahora (una sola consulta de agregación). */
    @Transactional(readOnly = true)
    public List<BudgetResponse> findByMonth(Long userId, int year, int month) {
        YearMonth period = YearMonth.of(year, month);
        Map<Long, BigDecimal> spentByCategory = transactionRepository
                .totalsByCategory(userId, CategoryType.EXPENSE, period.atDay(1), period.atEndOfMonth())
                .stream()
                .collect(Collectors.toMap(CategoryTotal::categoryId, CategoryTotal::total));

        return budgetRepository.findByUserIdAndYearAndMonth(userId, year, month).stream()
                .map(b -> toResponse(b, spentByCategory.getOrDefault(b.getCategory().getId(), BigDecimal.ZERO)))
                .sorted(Comparator.comparing(BudgetResponse::percentUsed).reversed())
                .toList();
    }

    @Transactional
    public BudgetResponse create(Long userId, BudgetRequest request) {
        Category category = categoryService.getOwned(userId, request.categoryId());
        if (category.getType() != CategoryType.EXPENSE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Solo se pueden presupuestar categorías de gasto");
        }
        if (budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonth(
                userId, category.getId(), request.year(), request.month())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya existe un presupuesto para esa categoría en ese mes");
        }
        Budget budget = budgetRepository.save(new Budget(userRepository.getReferenceById(userId), category,
                request.year(), request.month(), request.limit()));
        return toResponse(budget, spentFor(userId, budget));
    }

    @Transactional
    public BudgetResponse updateLimit(Long userId, Long id, BudgetLimitRequest request) {
        Budget budget = getOwned(userId, id);
        budget.updateLimit(request.limit());
        return toResponse(budget, spentFor(userId, budget));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        budgetRepository.delete(getOwned(userId, id));
    }

    private Budget getOwned(Long userId, Long id) {
        return budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto", id));
    }

    private BigDecimal spentFor(Long userId, Budget budget) {
        YearMonth period = YearMonth.of(budget.getYear(), budget.getMonth());
        return transactionRepository.sumByCategory(userId, budget.getCategory().getId(),
                period.atDay(1), period.atEndOfMonth());
    }

    static BudgetResponse toResponse(Budget budget, BigDecimal spent) {
        BigDecimal limit = budget.getLimit();
        BigDecimal percent = spent.multiply(BigDecimal.valueOf(100)).divide(limit, 1, RoundingMode.HALF_UP);
        return new BudgetResponse(
                budget.getId(),
                CategoryResponse.from(budget.getCategory()),
                budget.getYear(),
                budget.getMonth(),
                limit,
                spent,
                limit.subtract(spent),
                percent,
                BudgetStatus.fromPercentage(percent));
    }
}
