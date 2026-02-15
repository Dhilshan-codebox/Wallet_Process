package com.paypal.wallet.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ✅ Disables CSRF - Essential for REST APIs
            .csrf(csrf -> csrf.disable())

            // ✅ Allows public access to /auth/** endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            )

            // ✅ Disables default login page
            .formLogin(form -> form.disable())

            // ✅ Disables HTTP Basic authentication popup
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}