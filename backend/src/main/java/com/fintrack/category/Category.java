package com.fintrack.category;

import com.fintrack.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryType type;

    @Column(nullable = false, length = 7)
    private String color;

    @Column(length = 40)
    private String icon;

    protected Category() {
    }

    public Category(User user, String name, CategoryType type, String color, String icon) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.color = color;
        this.icon = icon;
    }

    public void update(String name, CategoryType type, String color, String icon) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.icon = icon;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }
}
