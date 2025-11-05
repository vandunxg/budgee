package com.budgee.config;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.budgee.service.impl.auth.UserDetailService;

@Configuration
@RequiredArgsConstructor
@Slf4j(topic = "WEB-SECURITY-CONFIG")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSecurityConfig {

    // -------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------

    UserDetailService userDetailService;
    CustomizeRequestFilter customizeRequestFilter;

    // -------------------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------------------

    String[] PUBLIC_ENDPOINT = {"/auth/**"};

    // -------------------------------------------------------------------
    // CONFIGS
    // -------------------------------------------------------------------

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("----------------[FILTER - CHAIN]----------------");

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        request -> {
                            request.requestMatchers(PUBLIC_ENDPOINT)
                                    .permitAll()
                                    .anyRequest()
                                    .authenticated();
                        })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        customizeRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider =
                new DaoAuthenticationProvider(userDetailService.userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

        return daoAuthenticationProvider;
    }

    // enable for swagger
    @Bean
    public WebSecurityCustomizer ignoreResources() {

        return webSecurity ->
                webSecurity
                        .ignoring()
                        .requestMatchers(
                                "/actuator/**",
                                "/v3/**",
                                "/webjars/**",
                                "/swagger-ui*/*swagger-initializer.js",
                                "/swagger-ui*/**",
                                "/favicon.ico");
    }

    // config cors
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}
