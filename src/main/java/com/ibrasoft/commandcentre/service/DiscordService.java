package com.ibrasoft.commandcentre.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibrasoft.commandcentre.config.DiscordConfig;
import com.ibrasoft.commandcentre.model.DiscordGuild;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordService {
    
    private final DiscordConfig discordConfig;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String DISCORD_API_BASE_URL = "https://discord.com/api/v10";
    
    public boolean isUserInRequiredGuild(String username) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                "discord", username
            );
            
            if (client == null) {
                log.warn("No authorized client found for user: {}", username);
                return false;
            }
            
            OAuth2AccessToken accessToken = client.getAccessToken();
            List<DiscordGuild> guilds = getUserGuilds(accessToken.getTokenValue());
            
            String requiredGuildId = discordConfig.getId();
            boolean isMember = guilds.stream()
                .anyMatch(guild -> guild.getId().equals(requiredGuildId));
            
            if (!isMember) {
                log.warn("User {} is not a member of required guild {}", username, requiredGuildId);
            }
            
            return isMember;
            
        } catch (Exception e) {
            log.error("Error checking guild membership for user: {}", username, e);
            return false;
        }
    }
    
    public List<DiscordGuild> getUserGuilds(String accessToken) {
        try {
            RestClient restClient = RestClient.create();
            
            String response = restClient.get()
                .uri(DISCORD_API_BASE_URL + "/users/@me/guilds")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.value() == 429, (req, res) -> {
                    log.warn("Discord API rate limit hit, returning empty guild list");
                    throw new RuntimeException("Discord API rate limit exceeded");
                })
                .body(String.class);
            
            return objectMapper.readValue(response, new TypeReference<List<DiscordGuild>>() {});
            
        } catch (Exception e) {
            log.error("Error fetching user guilds from Discord: {}", e.getMessage());
            // Return empty list on error to prevent blocking access temporarily
            // In production, you might want to handle this differently
            throw new RuntimeException("Failed to fetch Discord guilds", e);
        }
    }
}
