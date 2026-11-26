package com.fintrack.transaction;

import com.fintrack.category.CategoryType;
import com.fintrack.report.dto.CategoryTotal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /** Carga la categoría en la misma consulta para evitar el problema N+1 al paginar. */
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.user.id = :userId and t.category.type = :type
              and t.date between :from and :to
            """)
    BigDecimal sumByType(@Param("userId") Long userId, @Param("type") CategoryType type,
                         @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select count(t) from Transaction t
            where t.user.id = :userId and t.date between :from and :to
            """)
    long countInPeriod(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select new com.fintrack.report.dto.CategoryTotal(c.id, c.name, c.color, sum(t.amount))
            from Transaction t join t.category c
            where t.user.id = :userId and c.type = :type and t.date between :from and :to
            group by c.id, c.name, c.color
            order by sum(t.amount) desc
            """)
    List<CategoryTotal> totalsByCategory(@Param("userId") Long userId, @Param("type") CategoryType type,
                                         @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.user.id = :userId and t.category.id = :categoryId
              and t.date between :from and :to
            """)
    BigDecimal sumByCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId,
                             @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select t from Transaction t join fetch t.category
            where t.user.id = :userId and t.date between :from and :to
            """)
    List<Transaction> findInPeriodWithCategory(@Param("userId") Long userId,
                                               @Param("from") LocalDate from, @Param("to") LocalDate to);
}
