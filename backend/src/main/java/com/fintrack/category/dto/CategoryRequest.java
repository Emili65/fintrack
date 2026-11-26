package com.fintrack.category.dto;

import com.fintrack.category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
        String name,

        @NotNull(message = "El tipo es obligatorio (INCOME o EXPENSE)")
        CategoryType type,

        @NotBlank(message = "El color es obligatorio")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe tener formato hexadecimal, ej. #22C55E")
        String color,

        @Size(max = 40)
        String icon
) {
}
