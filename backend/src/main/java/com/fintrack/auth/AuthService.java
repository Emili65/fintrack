package com.fintrack.auth;

import com.fintrack.auth.dto.AuthResponse;
import com.fintrack.auth.dto.LoginRequest;
import com.fintrack.auth.dto.RegisterRequest;
import com.fintrack.auth.dto.UserResponse;
import com.fintrack.category.CategoryService;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.security.JwtService;
import com.fintrack.user.User;
import com.fintrack.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CategoryService categoryService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, CategoryService categoryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.categoryService = categoryService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        User user = userRepository.save(new User(request.name().trim(), email, passwordEncoder.encode(request.password())));
        categoryService.createDefaultCategories(user);
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
