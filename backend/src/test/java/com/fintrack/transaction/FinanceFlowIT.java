package com.fintrack.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo completo: categorías → transacciones → reportes → presupuestos, y aislamiento entre usuarios. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinanceFlowIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String token;
    private long salaryId;
    private long foodId;

    @BeforeEach
    void setUp() throws Exception {
        token = registerAndGetToken();
        JsonNode categories = getJson("/api/categories", token);
        for (JsonNode c : categories) {
            if (c.get("name").asText().equals("Salario")) salaryId = c.get("id").asLong();
            if (c.get("name").asText().equals("Alimentación")) foodId = c.get("id").asLong();
        }
    }

    @Test
    void transactionsFeedReportsAndBudgets() throws Exception {
        LocalDate today = LocalDate.now();
        createTransaction(salaryId, "3000000.00", today, "Nómina");
        createTransaction(foodId, "250000.00", today, "Mercado");
        createTransaction(foodId, "50000.00", today, "Almuerzo");

        mvc.perform(get("/api/reports/summary").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(3000000.00))
                .andExpect(jsonPath("$.totalExpense").value(300000.00))
                .andExpect(jsonPath("$.balance").value(2700000.00))
                .andExpect(jsonPath("$.savingsRate").value(90.0))
                .andExpect(jsonPath("$.transactionCount").value(3));

        mvc.perform(get("/api/reports/by-category").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alimentación"))
                .andExpect(jsonPath("$[0].percentage").value(100.0));

        mvc.perform(get("/api/reports/monthly").param("year", String.valueOf(today.getYear()))
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[%d].expense".formatted(today.getMonthValue() - 1)).value(300000.00));

        mvc.perform(get("/api/transactions").param("search", "merc").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Mercado"));

        mvc.perform(get("/api/transactions").param("type", "EXPENSE").param("sort", "amount,asc")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].amount").value(50000.00));

        mvc.perform(post("/api/budgets").header("Authorization", bearer()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"year":%d,"month":%d,"limit":350000}
                                """.formatted(foodId, today.getYear(), today.getMonthValue())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spent").value(300000.00))
                .andExpect(jsonPath("$.status").value("WARNING"));

        // No se pueden presupuestar ingresos
        mvc.perform(post("/api/budgets").header("Authorization", bearer()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"year":%d,"month":%d,"limit":100}
                                """.formatted(salaryId, today.getYear(), today.getMonthValue())))
                .andExpect(status().isBadRequest());

        // Una categoría con transacciones no se puede borrar
        mvc.perform(delete("/api/categories/" + foodId).header("Authorization", bearer()))
                .andExpect(status().isConflict());
    }

    @Test
    void usersCannotSeeOrUseOtherUsersData() throws Exception {
        long txId = createTransaction(foodId, "10000", LocalDate.now(), "Privada");
        String intruder = registerAndGetToken();

        mvc.perform(get("/api/transactions/" + txId).header("Authorization", "Bearer " + intruder))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/transactions").header("Authorization", "Bearer " + intruder)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"amount":5000,"date":"%s"}
                                """.formatted(foodId, LocalDate.now())))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/transactions").header("Authorization", "Bearer " + intruder))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void invalidTransactionIsRejected() throws Exception {
        mvc.perform(post("/api/transactions").header("Authorization", bearer()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"amount":-5,"date":"%s"}
                                """.formatted(foodId, LocalDate.now().plusDays(3))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists())
                .andExpect(jsonPath("$.fieldErrors.date").exists());
    }

    @Test
    void updateAndDeleteTransaction() throws Exception {
        long id = createTransaction(foodId, "20000", LocalDate.now(), "Café");

        mvc.perform(put("/api/transactions/" + id).header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"amount":25000,"date":"%s","description":"Café y pan"}
                                """.formatted(foodId, LocalDate.now())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25000))
                .andExpect(jsonPath("$.description").value("Café y pan"));

        mvc.perform(delete("/api/transactions/" + id).header("Authorization", bearer()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/transactions/" + id).header("Authorization", bearer()))
                .andExpect(status().isNotFound());
    }

    private long createTransaction(long categoryId, String amount, LocalDate date, String description) throws Exception {
        String body = mvc.perform(post("/api/transactions").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"amount":%s,"date":"%s","description":"%s"}
                                """.formatted(categoryId, amount, date, description)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asLong();
    }

    private String registerAndGetToken() throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","email":"flow-%s@test.com","password":"Secreta123"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
    }

    private JsonNode getJson(String url, String jwt) throws Exception {
        String body = mvc.perform(get(url).header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    private String bearer() {
        return "Bearer " + token;
    }
}
