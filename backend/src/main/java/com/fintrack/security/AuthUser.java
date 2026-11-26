package com.fintrack.security;

/** Usuario autenticado extraído del JWT. Se inyecta con @AuthenticationPrincipal. */
public record AuthUser(Long id, String email) {
}
