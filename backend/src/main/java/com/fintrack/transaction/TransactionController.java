package com.fintrack.transaction;

import com.fintrack.category.CategoryType;
import com.fintrack.common.PageResponse;
import com.fintrack.security.AuthUser;
import com.fintrack.transaction.dto.TransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transacciones")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Listar transacciones con filtros y paginación (sort: date, amount)")
    public PageResponse<TransactionResponse> list(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = {"date", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        var filter = new TransactionFilter(from, to, categoryId, type, search);
        return transactionService.search(user.id(), filter, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una transacción")
    public TransactionResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return transactionService.findById(user.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un ingreso o gasto (el tipo lo define la categoría)")
    public TransactionResponse create(@AuthenticationPrincipal AuthUser user,
                                      @Valid @RequestBody TransactionRequest request) {
        return transactionService.create(user.id(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una transacción")
    public TransactionResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                      @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una transacción")
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        transactionService.delete(user.id(), id);
    }
}
