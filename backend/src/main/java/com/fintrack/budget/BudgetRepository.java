package com.fintrack.budget;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @EntityGraph(attributePaths = "category")
    List<Budget> findByUserIdAndYearAndMonth(Long userId, int year, int month);

    @EntityGraph(attributePaths = "category")
    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndCategoryIdAndYearAndMonth(Long userId, Long categoryId, int year, int month);
}
