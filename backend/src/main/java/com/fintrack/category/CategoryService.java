package com.fintrack.category;

import com.fintrack.category.dto.CategoryRequest;
import com.fintrack.category.dto.CategoryResponse;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.transaction.TransactionRepository;
import com.fintrack.user.User;
import com.fintrack.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    /** Categorías con las que arranca todo usuario nuevo. */
    private static final List<CategoryRequest> DEFAULTS = List.of(
            new CategoryRequest("Salario", CategoryType.INCOME, "#16A34A", "briefcase"),
            new CategoryRequest("Freelance", CategoryType.INCOME, "#0D9488", "laptop"),
            new CategoryRequest("Otros ingresos", CategoryType.INCOME, "#65A30D", "plus"),
            new CategoryRequest("Alimentación", CategoryType.EXPENSE, "#F97316", "cart"),
            new CategoryRequest("Transporte", CategoryType.EXPENSE, "#3B82F6", "bus"),
            new CategoryRequest("Vivienda", CategoryType.EXPENSE, "#8B5CF6", "home"),
            new CategoryRequest("Servicios", CategoryType.EXPENSE, "#06B6D4", "bolt"),
            new CategoryRequest("Entretenimiento", CategoryType.EXPENSE, "#EC4899", "film"),
            new CategoryRequest("Salud", CategoryType.EXPENSE, "#EF4444", "heart"),
            new CategoryRequest("Educación", CategoryType.EXPENSE, "#EAB308", "book")
    );

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(Long userId, CategoryType type) {
        List<Category> categories = type == null
                ? categoryRepository.findByUserIdOrderByTypeAscNameAsc(userId)
                : categoryRepository.findByUserIdAndTypeOrderByNameAsc(userId, type);
        return categories.stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByUserIdAndTypeAndNameIgnoreCase(userId, request.type(), name)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya tienes una categoría con ese nombre y tipo");
        }
        Category category = new Category(userRepository.getReferenceById(userId), name,
                request.type(), request.color().toUpperCase(), request.icon());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long userId, Long id, CategoryRequest request) {
        Category category = getOwned(userId, id);
        String name = request.name().trim();
        if (categoryRepository.existsByUserIdAndTypeAndNameIgnoreCaseAndIdNot(userId, request.type(), name, id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya tienes una categoría con ese nombre y tipo");
        }
        if (category.getType() != request.type() && transactionRepository.existsByCategoryId(id)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "No se puede cambiar el tipo de una categoría que ya tiene transacciones");
        }
        category.update(name, request.type(), request.color().toUpperCase(), request.icon());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Category category = getOwned(userId, id);
        if (transactionRepository.existsByCategoryId(id)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "No se puede eliminar: la categoría tiene transacciones asociadas");
        }
        categoryRepository.delete(category);
    }

    /** Busca una categoría garantizando que pertenezca al usuario (evita acceso a datos ajenos). */
    @Transactional(readOnly = true)
    public Category getOwned(Long userId, Long id) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
    }

    @Transactional
    public void createDefaultCategories(User user) {
        List<Category> categories = DEFAULTS.stream()
                .map(d -> new Category(user, d.name(), d.type(), d.color(), d.icon()))
                .toList();
        categoryRepository.saveAll(categories);
    }
}
