package com.smartpool.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, Environment env) throws Exception {
        boolean enabled = Boolean.parseBoolean(env.getProperty("SMARTPOOL_AUTH_ENABLED", "false"));
        boolean production = Arrays.asList(env.getActiveProfiles()).contains("prod");
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieCustomizer(cookie -> cookie.sameSite(production ? "None" : "Lax").secure(production).path("/"));
        if (!enabled) {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else {
            http.cors(cors -> {})
                    .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/register", "/api/csrf", "/health", "/error").permitAll()
                            .anyRequest().authenticated())
                    .formLogin(form -> form.loginPage("/login.html")
                            .loginProcessingUrl("/login")
                            .defaultSuccessUrl("/", true)
                            .failureUrl("/login.html?error")
                            .permitAll())
                    .logout(logout -> logout.logoutSuccessUrl("/login.html?loggedOut"));
        }
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(Environment env) {
        List<String> origins = Arrays.stream(env.getProperty("SMARTPOOL_FRONTEND_ORIGIN", "http://localhost:5500").split(","))
                .map(String::trim).filter(origin -> !origin.isEmpty()).toList();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "X-CSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(Environment env, PasswordEncoder encoder,
            com.smartpool.backend.repository.AccountRepository accounts) {
        boolean enabled = Boolean.parseBoolean(env.getProperty("SMARTPOOL_AUTH_ENABLED", "false"));
        if (!enabled) return new InMemoryUserDetailsManager();

        String username = env.getProperty("SMARTPOOL_AUTH_USERNAME", "").trim();
        String password = env.getProperty("SMARTPOOL_AUTH_PASSWORD", "");
        if (username.isEmpty() || password.length() < 16)
            throw new IllegalStateException("Set SMARTPOOL_AUTH_USERNAME and a SMARTPOOL_AUTH_PASSWORD of at least 16 characters.");
        String adminHash = encoder.encode(password);
        return login -> {
            if (username.equalsIgnoreCase(login))
                return User.withUsername(username).password(adminHash).roles("ADMIN").build();
            return accounts.findByEmailIgnoreCase(login.trim())
                    .map(account -> User.withUsername(account.email).password(account.passwordHash).roles("USER").build())
                    .orElseThrow(() -> new UsernameNotFoundException("No existe una cuenta con ese correo."));
        };
    }
}
