package com.skillverse.skillverse_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/public/**", "/api/auth/**", "/login/oauth2/**", "/api/marketplace/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService())
                        )
                        .defaultSuccessUrl("/api/auth/success", true)
                        .failureUrl("/api/auth/failure")
                        .successHandler((request, response, authentication) -> {
                            response.sendRedirect("/api/auth/success");
                        })
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.getWriter().write("{\"message\": \"Login successful\", \"email\": \"" + authentication.getName() + "\"}");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(401);
                            response.getWriter().write("{\"error\": \"Login failed: " + exception.getMessage() + "\"}");
                        })
                );
        return http.build();
    }

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}