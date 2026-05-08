package com.example.grade.config;

import com.example.grade.common.ErrorCode;
import com.example.grade.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.security.authentication.DisabledException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/logout", "/auth/register").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/auth/login")
                .successHandler((req, resp, auth) -> writeJson(resp, Result.success(auth.getPrincipal())))
                .failureHandler((req, resp, ex) -> {
                    if (ex instanceof DisabledException) {
                        writeJson(resp, Result.error(ErrorCode.ACCOUNT_DISABLED));
                    } else {
                        writeJson(resp, Result.error(ErrorCode.PASSWORD_ERROR));
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessHandler((req, resp, auth) -> writeJson(resp, Result.success("Logged out")))
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, resp, authEx) -> {
                    // 返回 HTTP 200，错误码在响应体中
                    writeJson(resp, Result.error(ErrorCode.UNAUTHORIZED));
                })
                .accessDeniedHandler((req, resp, accessEx) -> {
                    // 返回 HTTP 200，错误码在响应体中
                    writeJson(resp, Result.error(ErrorCode.FORBIDDEN));
                })
            );

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        // SUPER_ADMIN > TEACHER/ STUDENT
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("""
            ROLE_SUPER_ADMIN > ROLE_TEACHER
            ROLE_SUPER_ADMIN > ROLE_STUDENT
        """);
        return hierarchy;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration obj = new CorsConfiguration();
        obj.setAllowedOriginPatterns(List.of("*"));
        obj.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        obj.setAllowedHeaders(List.of("*"));
        obj.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", obj);
        return source;
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        out.print(mapper.writeValueAsString(data));
        out.flush();
    }
}
