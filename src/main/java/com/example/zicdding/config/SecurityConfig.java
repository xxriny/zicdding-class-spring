package com.example.zicdding.config;

import com.example.zicdding.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.CustomAutowireConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder matcher = new MvcRequestMatcher.Builder(introspector);

        MvcRequestMatcher[] premitAllWhilteList = {
                matcher.pattern("/swagger-ui/**"),
                matcher.pattern("/v3/api-docs/**"),
                matcher.pattern("/users/**"),
                matcher.pattern("/users/login/**"),
                matcher.pattern("/users/signUp/**"),
                matcher.pattern("/classes/**")
        };

          http
                  .csrf(AbstractHttpConfigurer::disable)
                  .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(premitAllWhilteList).permitAll()
                //.requestMatchers(HttpMethod.DELETE, "/users").hasRole(RoleName.ROLE_ADMIN.get)
                .anyRequest().authenticated());

          return http.build();

    }

}
