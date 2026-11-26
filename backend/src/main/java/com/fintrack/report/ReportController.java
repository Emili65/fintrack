package com.fintrack.report;

import com.fintrack.category.CategoryType;
import com.fintrack.report.dto.CategoryBreakdown;
import com.fintrack.report.dto.MonthlyPoint;
import com.fintrack.report.dto.SummaryResponse;
import com.fintrack.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reportes")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Totales de ingresos, gastos, balance y tasa de ahorro (por defecto, el mes actual)")
    public SummaryResponse summary(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        YearMonth current = YearMonth.now();
        return reportService.summary(user.id(),
                from != null ? from : current.atDay(1),
                to != null ? to : current.atEndOfMonth());
    }

    @GetMapping("/by-category")
    @Operation(summary = "Distribución por categoría para un tipo (por defecto gastos del mes actual)")
    public List<CategoryBreakdown> byCategory(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "EXPENSE") CategoryType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        YearMonth current = YearMonth.now();
        return reportService.byCategory(user.id(), type,
                from != null ? from : current.atDay(1),
                to != null ? to : current.atEndOfMonth());
    }

    @GetMapping("/monthly")
    @Operation(summary = "Ingresos y gastos de los 12 meses de un año")
    public List<MonthlyPoint> monthly(@AuthenticationPrincipal AuthUser user,
                                      @RequestParam @Min(2000) @Max(2100) int year) {
        return reportService.monthly(user.id(), year);
    }
}
