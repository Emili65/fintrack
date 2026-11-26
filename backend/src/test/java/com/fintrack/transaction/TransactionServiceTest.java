package com.fintrack.transaction;

import com.fintrack.category.CategoryService;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.transaction.dto.TransactionRequest;
import com.fintrack.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService service;

    @Test
    void createWithForeignCategoryFailsAndSavesNothing() {
        when(categoryService.getOwned(1L, 99L)).thenThrow(new ResourceNotFoundException("Categoría", 99L));
        var request = new TransactionRequest(99L, BigDecimal.TEN, LocalDate.now(), "x");

        assertThatThrownBy(() -> service.create(1L, request)).isInstanceOf(ResourceNotFoundException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deletingUnknownTransactionThrowsNotFound() {
        when(transactionRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L, 5L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void invertedDateRangeIsRejected() {
        var filter = new TransactionFilter(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1), null, null, null);

        assertThatThrownBy(() -> service.search(1L, filter, Pageable.unpaged()))
                .isInstanceOf(BusinessException.class);
    }
}
