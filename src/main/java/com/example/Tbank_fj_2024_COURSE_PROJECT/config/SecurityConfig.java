package com.example.Tbank_fj_2024_COURSE_PROJECT.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/webhook")) // Отключить CSRF для вебхука
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/webhook").permitAll() // Разрешить доступ только к вебхуку
                        .anyRequest().denyAll() // Все остальные запросы запрещены
                );
        return http.build();
    }
}
