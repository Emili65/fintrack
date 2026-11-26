package com.fintrack.transaction;

import com.fintrack.category.Category;
import com.fintrack.category.CategoryService;
import com.fintrack.common.PageResponse;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.transaction.dto.TransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import com.fintrack.user.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryService categoryService,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> search(Long userId, TransactionFilter filter, Pageable pageable) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La fecha inicial no puede ser posterior a la final");
        }
        return PageResponse.from(transactionRepository
                .findAll(TransactionSpecifications.withFilter(userId, filter), pageable)
                .map(TransactionResponse::from));
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long userId, Long id) {
        return TransactionResponse.from(getOwned(userId, id));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Category category = categoryService.getOwned(userId, request.categoryId());
        Transaction transaction = new Transaction(userRepository.getReferenceById(userId), category,
                request.amount(), request.date(), clean(request.description()));
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = getOwned(userId, id);
        Category category = categoryService.getOwned(userId, request.categoryId());
        transaction.update(category, request.amount(), request.date(), clean(request.description()));
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        transactionRepository.delete(getOwned(userId, id));
    }

    private Transaction getOwned(Long userId, Long id) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción", id));
    }

    private static String clean(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
