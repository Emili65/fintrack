package com.fintrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// La autenticación es 100% JWT: no se necesita el usuario en memoria por defecto de Spring Boot
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class FintrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FintrackApplication.class, args);
    }
}
