package com.fintrack.seed;

import com.fintrack.budget.Budget;
import com.fintrack.budget.BudgetRepository;
import com.fintrack.category.Category;
import com.fintrack.category.CategoryRepository;
import com.fintrack.category.CategoryService;
import com.fintrack.transaction.Transaction;
import com.fintrack.transaction.TransactionRepository;
import com.fintrack.user.User;
import com.fintrack.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Crea un usuario de demostración con 6 meses de movimientos para poder probar la app al instante.
 * Se activa con SEED_DEMO=true (ya viene activado en docker-compose).
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    public static final String DEMO_EMAIL = "demo@fintrack.dev";
    public static final String DEMO_PASSWORD = "Demo1234";

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository, CategoryRepository categoryRepository,
                          CategoryService categoryService, TransactionRepository transactionRepository,
                          BudgetRepository budgetRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase(DEMO_EMAIL)) {
            return;
        }
        User user = userRepository.save(new User("Usuario Demo", DEMO_EMAIL, passwordEncoder.encode(DEMO_PASSWORD)));
        categoryService.createDefaultCategories(user);
        Map<String, Category> cat = categoryRepository.findByUserIdOrderByTypeAscNameAsc(user.getId()).stream()
                .collect(Collectors.toMap(Category::getName, Function.identity()));

        Random random = new Random(42); // semilla fija: los datos de demo siempre son iguales
        LocalDate today = LocalDate.now();
        List<Transaction> transactions = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today).minusMonths(i);
            int lastDay = ym.equals(YearMonth.from(today)) ? today.getDayOfMonth() : ym.lengthOfMonth();

            add(transactions, user, cat.get("Salario"), 3_800_000, ym.atDay(1), "Pago de nómina");
            if (random.nextBoolean()) {
                add(transactions, user, cat.get("Freelance"), 400_000 + random.nextInt(8) * 100_000,
                        ym.atDay(Math.min(15, lastDay)), "Proyecto freelance");
            }
            add(transactions, user, cat.get("Vivienda"), 1_200_000, ym.atDay(Math.min(5, lastDay)), "Arriendo");
            add(transactions, user, cat.get("Servicios"), 180_000 + random.nextInt(60_000),
                    ym.atDay(Math.min(10, lastDay)), "Agua, luz e internet");

            for (int d = 1; d <= lastDay; d += 3 + random.nextInt(3)) {
                add(transactions, user, cat.get("Alimentación"), 35_000 + random.nextInt(120_000),
                        ym.atDay(d), pick(random, "Mercado", "Restaurante", "Panadería", "Domicilio"));
            }
            for (int d = 2; d <= lastDay; d += 4 + random.nextInt(4)) {
                add(transactions, user, cat.get("Transporte"), 8_000 + random.nextInt(40_000),
                        ym.atDay(d), pick(random, "Bus", "Taxi", "Gasolina"));
            }
            add(transactions, user, cat.get("Entretenimiento"), 50_000 + random.nextInt(150_000),
                    ym.atDay(Math.min(20, lastDay)), pick(random, "Cine", "Streaming", "Salida con amigos"));
            if (random.nextInt(3) == 0) {
                add(transactions, user, cat.get("Salud"), 60_000 + random.nextInt(100_000),
                        ym.atDay(Math.min(12, lastDay)), "Farmacia");
            }
        }
        transactionRepository.saveAll(transactions);

        YearMonth current = YearMonth.from(today);
        budgetRepository.saveAll(List.of(
                new Budget(user, cat.get("Alimentación"), current.getYear(), current.getMonthValue(), new BigDecimal("900000")),
                new Budget(user, cat.get("Transporte"), current.getYear(), current.getMonthValue(), new BigDecimal("200000")),
                new Budget(user, cat.get("Entretenimiento"), current.getYear(), current.getMonthValue(), new BigDecimal("150000"))));

        log.info("Usuario demo creado: {} / {} ({} transacciones)", DEMO_EMAIL, DEMO_PASSWORD, transactions.size());
    }

    private static void add(List<Transaction> list, User user, Category category, int amount,
                            LocalDate date, String description) {
        // Montos redondeados a centenas, como en pesos colombianos reales
        BigDecimal value = BigDecimal.valueOf(Math.round(amount / 100.0) * 100L);
        list.add(new Transaction(user, category, value, date, description));
    }

    private static String pick(Random random, String... options) {
        return options[random.nextInt(options.length)];
    }
}
