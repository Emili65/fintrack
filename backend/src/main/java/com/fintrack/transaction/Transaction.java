package com.fintrack.transaction;

import com.fintrack.category.Category;
import com.fintrack.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Transaction() {
    }

    public Transaction(User user, Category category, BigDecimal amount, LocalDate date, String description) {
        this.user = user;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void update(Category category, BigDecimal amount, LocalDate date, String description) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Category getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
