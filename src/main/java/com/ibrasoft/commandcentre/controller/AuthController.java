package com.ibrasoft.commandcentre.controller;

import com.ibrasoft.commandcentre.model.DiscordGuild;
import com.ibrasoft.commandcentre.model.Role;
import com.ibrasoft.commandcentre.security.JwtService;
import com.ibrasoft.commandcentre.service.DiscordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final DiscordService discordService;
    private final JwtService jwtService;
    
    @GetMapping("/success")
    public ResponseEntity<Map<String, Object>> loginSuccess(Authentication authentication) {
        OAuth2User principal = authentication != null && authentication.getPrincipal() instanceof OAuth2User user
            ? user
            : null;
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        String userId = principal.getAttribute("id");
        String token = jwtService.generateUserToken(userId, List.of(Role.ROLE_USER));
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Successfully authenticated with Discord");
        response.put("token", token);
        response.put("user", Map.of(
            "id", principal.getAttribute("id"),
            "username", principal.getAttribute("username"),
            "discriminator", principal.getAttribute("discriminator"),
            "avatar", principal.getAttribute("avatar")
        ));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/failure")
    public ResponseEntity<Map<String, Object>> loginFailure() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Authentication with Discord failed");
        return ResponseEntity.status(401).body(response);
    }
    
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", oauth2User.getAttribute("id"));
            response.put("username", oauth2User.getAttribute("username"));
            response.put("discriminator", oauth2User.getAttribute("discriminator"));
            response.put("avatar", oauth2User.getAttribute("avatar"));
            response.put("email", oauth2User.getAttribute("email"));
            return ResponseEntity.ok(response);
        }
        if (principal instanceof String userId) {
            // This handles JWT authentication where the principal is the subject (user ID)
            return ResponseEntity.ok(Map.of("id", userId));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }
    
    @GetMapping("/guilds")
    public ResponseEntity<List<DiscordGuild>> getUserGuilds(Authentication authentication) {
        OAuth2User principal = authentication != null && authentication.getPrincipal() instanceof OAuth2User user
            ? user
            : null;
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        String userId = principal.getAttribute("id");
        try {
            // This requires the user's access token which we can get from the authorized client
            org.springframework.security.oauth2.client.OAuth2AuthorizedClient client = 
                getAuthorizedClient(userId);
            
            if (client != null) {
                List<DiscordGuild> guilds = discordService.getUserGuilds(
                    client.getAccessToken().getTokenValue()
                );
                return ResponseEntity.ok(guilds);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
        
        return ResponseEntity.status(500).build();
    }
    
    // Helper method - you'll need to inject OAuth2AuthorizedClientService
    private org.springframework.security.oauth2.client.OAuth2AuthorizedClient getAuthorizedClient(String principalName) {
        // This is a placeholder - in production you'd inject OAuth2AuthorizedClientService
        return null;
    }
}
