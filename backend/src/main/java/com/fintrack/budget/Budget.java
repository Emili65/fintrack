package com.fintrack.budget;

import com.fintrack.category.Category;
import com.fintrack.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "budget_year", nullable = false)
    private int year;

    @Column(name = "budget_month", nullable = false)
    private int month;

    @Column(name = "amount_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal limit;

    protected Budget() {
    }

    public Budget(User user, Category category, int year, int month, BigDecimal limit) {
        this.user = user;
        this.category = category;
        this.year = year;
        this.month = month;
        this.limit = limit;
    }

    public void updateLimit(BigDecimal limit) {
        this.limit = limit;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Category getCategory() { return category; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public BigDecimal getLimit() { return limit; }
}
