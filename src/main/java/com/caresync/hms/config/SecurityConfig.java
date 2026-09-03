package com.caresync.hms.config;

import com.caresync.hms.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Order matters here: requestMatchers are evaluated top-to-bottom and the
                // first match wins. "/api/patients/{id}" is the only patient endpoint a
                // PATIENT is allowed to reach directly, and only because PatientService
                // additionally checks that the id belongs to them -- every endpoint that
                // returns a list of patients (all, search, by-status) must be matched
                // *before* the generic {id} rule, or that single-segment wildcard would
                // swallow "/api/patients/search" too and hand a PATIENT the whole roster.
                .requestMatchers(HttpMethod.GET, "/api/patients/search").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/patients/status/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/patients/{id}").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                .requestMatchers(HttpMethod.GET, "/api/patients").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/doctors/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                .requestMatchers("/api/doctors/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/departments/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                .requestMatchers("/api/departments/**").hasRole("ADMIN")
                .requestMatchers("/api/appointments/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/invoices/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/api/emergency/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/api/dashboard/**").authenticated()
                .requestMatchers("/api/ehr-documents/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
