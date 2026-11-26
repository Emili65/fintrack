package com.fintrack.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIdOrderByTypeAscNameAsc(Long userId);

    List<Category> findByUserIdAndTypeOrderByNameAsc(Long userId, CategoryType type);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndTypeAndNameIgnoreCase(Long userId, CategoryType type, String name);

    boolean existsByUserIdAndTypeAndNameIgnoreCaseAndIdNot(Long userId, CategoryType type, String name, Long id);
}
