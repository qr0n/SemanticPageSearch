package com.sitewatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 *
 * Configures Spring Security for the application.
 * Currently allows all requests for development; production should enable JWT authentication.
 *
 * Security features (to be implemented):
 * - JWT token validation for user authentication
 * - API key authentication for server-to-server calls
 * - CSRF protection for state-changing operations
 * - Rate limiting per IP address
 *
 * @author Infinity Iron
 * @since 0.1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure HTTP security with authentication and authorization rules.
     *
     * Current configuration:
     * - Permits all requests (development mode)
     * - Stateless sessions (for REST API)
     * - CSRF disabled (for REST API)
     *
     * Production configuration should:
     * - Require JWT authentication for /api/v1/** endpoints
     * - Allow anonymous access to /actuator/health
     * - Require specific roles for admin operations
     *
     * @param http the HTTP security configuration
     * @return the configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/sources").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/sources/**").permitAll()
                .anyRequest().permitAll()  // TODO: Enable JWT authentication in production
            );

        return http.build();
    }
}
