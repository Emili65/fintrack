package com.fintrack.transaction;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

/** Construye la consulta dinámica según los filtros que envíe el cliente. */
final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    static Specification<Transaction> withFilter(Long userId, TransactionFilter f) {
        Specification<Transaction> spec = (root, q, cb) -> cb.equal(root.get("user").get("id"), userId);

        if (f.from() != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.<LocalDate>get("date"), f.from()));
        }
        if (f.to() != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.<LocalDate>get("date"), f.to()));
        }
        if (f.categoryId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("category").get("id"), f.categoryId()));
        }
        if (f.type() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("category").get("type"), f.type()));
        }
        if (f.search() != null && !f.search().isBlank()) {
            String pattern = "%" + f.search().trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.<String>get("description")), pattern));
        }
        return spec;
    }
}
