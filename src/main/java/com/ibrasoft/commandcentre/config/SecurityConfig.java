package com.ibrasoft.commandcentre.config;

import com.ibrasoft.commandcentre.security.DiscordGuildFilter;
import com.ibrasoft.commandcentre.security.DiscordOAuthSuccessHandler;
import com.ibrasoft.commandcentre.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final DiscordGuildFilter discordGuildFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DiscordOAuthSuccessHandler discordOAuthSuccessHandler;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http)) // Enable CORS support in Spring Security
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API usage
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            // Role model (JWT roles claim): ROLE_USER = dashboard, read-only; ROLE_BOT = Discord bot, full access.
            // Rules are evaluated top to bottom; the first match wins, so keep the most specific ones first.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login/**", "/oauth2/**", "/error", "/api/auth/**", "/v3/api-docs").permitAll()

                // Read-only lookups the dashboard sends as POST because they carry a list body.
                .requestMatchers(HttpMethod.POST, "/api/discord/users/bulk", "/api/discord/roles/bulk")
                    .hasAnyRole("USER", "BOT")
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "BOT")
                // Every other method under /api/** (POST, PUT, PATCH, DELETE, ...) is bot-only.
                .requestMatchers("/api/**").hasRole("BOT")

                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(discordOAuthSuccessHandler)
                .failureUrl(frontendUrl + "/unauthorized?reason=auth_failed")
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(discordGuildFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
