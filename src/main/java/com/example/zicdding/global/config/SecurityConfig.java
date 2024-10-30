package com.example.zicdding.global.config;

import com.example.zicdding.security.filter.JwtAuthFilter;
import com.example.zicdding.security.handler.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final HandlerMappingIntrospector introspector;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private CustomAccessDeniedHandler customAccessDeniedHandler;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector, JwtAuthFilter jwtAuthFilter) throws Exception {
        MvcRequestMatcher.Builder matcher = new MvcRequestMatcher.Builder(introspector);

        MvcRequestMatcher[] permitAllWhitelist = {
                matcher.pattern("/swagger-ui/**"),
                matcher.pattern("/v3/api-docs/**"),
                matcher.pattern("/users/**"),
                matcher.pattern("/classes/**"),
                matcher.pattern("/signUp.html"),
                matcher.pattern("/test/**"),
        };
          http
                  .csrf(AbstractHttpConfigurer::disable)
                  .authorizeHttpRequests(authorize -> authorize
                          .requestMatchers(permitAllWhitelist).permitAll()
                          .anyRequest().authenticated())
                  .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                  .exceptionHandling(handling -> handling
                          .accessDeniedHandler(customAccessDeniedHandler));

          return http.build();
    }

}
