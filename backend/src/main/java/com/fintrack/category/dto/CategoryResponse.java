package com.fintrack.category.dto;

import com.fintrack.category.Category;
import com.fintrack.category.CategoryType;

public record CategoryResponse(Long id, String name, CategoryType type, String color, String icon) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getType(), c.getColor(), c.getIcon());
    }
}
