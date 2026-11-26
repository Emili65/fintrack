package com.fintrack.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void registerLoginAndGetProfile() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@test.com";

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","email":"%s","password":"Secreta123"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email));

        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Secreta123"}
                                """.formatted(email.toUpperCase())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(body).get("token").asText();

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"));

        // El registro crea categorías por defecto
        mvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    void duplicatedEmailReturnsConflict() throws Exception {
        String payload = """
                {"name":"Ana","email":"dup-%s@test.com","password":"Secreta123"}
                """.formatted(UUID.randomUUID());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {
        String email = "wrong-" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Ana","email":"%s","password":"Secreta123"}
                        """.formatted(email)));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"otraClave"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidRegisterReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"no-es-correo","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasKey("email")))
                .andExpect(jsonPath("$.fieldErrors", hasKey("password")))
                .andExpect(jsonPath("$.fieldErrors", hasKey("name")));
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void tamperedTokenReturns401() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.e30.firma-falsa"))
                .andExpect(status().isUnauthorized());
    }
}
