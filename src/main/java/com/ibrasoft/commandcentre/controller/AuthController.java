package com.ibrasoft.commandcentre.controller;

import com.ibrasoft.commandcentre.model.DiscordGuild;
import com.ibrasoft.commandcentre.service.DiscordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    
    @GetMapping("/success")
    public ResponseEntity<Map<String, Object>> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Successfully authenticated with Discord");
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
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", principal.getAttribute("id"));
        response.put("username", principal.getAttribute("username"));
        response.put("discriminator", principal.getAttribute("discriminator"));
        response.put("avatar", principal.getAttribute("avatar"));
        response.put("email", principal.getAttribute("email"));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/guilds")
    public ResponseEntity<List<DiscordGuild>> getUserGuilds(@AuthenticationPrincipal OAuth2User principal) {
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
