package com.ibrasoft.commandcentre.security;

import com.ibrasoft.commandcentre.service.DiscordService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordGuildFilter extends OncePerRequestFilter {
    
    private final DiscordService discordService;
    private static final String GUILD_CHECK_ATTRIBUTE = "DISCORD_GUILD_VERIFIED";
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof OAuth2User) {
            
            HttpSession session = request.getSession(true);
            Boolean guildVerified = (Boolean) session.getAttribute(GUILD_CHECK_ATTRIBUTE);
            
            // Only check guild membership once per session
            if (guildVerified == null) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String userId = oauth2User.getAttribute("id");
                
                log.info("Checking guild membership for user: {}", userId);
                
                if (!discordService.isUserInRequiredGuild(userId)) {
                    log.warn("User {} is not in required guild", userId);
                    session.invalidate();
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"error\":\"Access Denied\",\"message\":\"You must be a member of the required Discord server to access this API\"}"
                    );
                    return;
                }
                
                // Cache the result in session
                session.setAttribute(GUILD_CHECK_ATTRIBUTE, Boolean.TRUE);
                log.info("User {} verified as guild member", userId);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Don't filter login endpoints and public resources
        String path = request.getServletPath();
        return path.startsWith("/login") || 
               path.startsWith("/oauth2") || 
               path.equals("/") ||
               path.startsWith("/error");
    }
}
