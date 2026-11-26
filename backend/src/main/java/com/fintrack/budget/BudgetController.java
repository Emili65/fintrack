package com.fintrack.budget;

import com.fintrack.budget.dto.BudgetLimitRequest;
import com.fintrack.budget.dto.BudgetRequest;
import com.fintrack.budget.dto.BudgetResponse;
import com.fintrack.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Presupuestos")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    @Operation(summary = "Presupuestos de un mes con lo gastado y el estado (OK, WARNING, EXCEEDED)")
    public List<BudgetResponse> list(@AuthenticationPrincipal AuthUser user,
                                     @RequestParam @Min(2000) @Max(2100) int year,
                                     @RequestParam @Min(1) @Max(12) int month) {
        return budgetService.findByMonth(user.id(), year, month);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un presupuesto mensual para una categoría de gasto")
    public BudgetResponse create(@AuthenticationPrincipal AuthUser user,
                                 @Valid @RequestBody BudgetRequest request) {
        return budgetService.create(user.id(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cambiar el límite de un presupuesto")
    public BudgetResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                 @Valid @RequestBody BudgetLimitRequest request) {
        return budgetService.updateLimit(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un presupuesto")
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        budgetService.delete(user.id(), id);
    }
}
