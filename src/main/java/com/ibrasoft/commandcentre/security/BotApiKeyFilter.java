package com.ibrasoft.commandcentre.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class BotApiKeyFilter extends OncePerRequestFilter {

    @Value("${bot.api.key}")
    private String botApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        // Check if request has valid bot API key
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String apiKey = authHeader.substring(7);
            
            if (botApiKey.equals(apiKey)) {
                log.debug("Valid bot API key detected, bypassing OAuth");
                
                // Create an authentication token for the bot
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        "discord-bot",
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_BOT"))
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
