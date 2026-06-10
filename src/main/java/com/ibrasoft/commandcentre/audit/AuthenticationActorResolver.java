package com.ibrasoft.commandcentre.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationActorResolver {

    public static class ActorResolutionException extends RuntimeException {
        public ActorResolutionException(String message) { super(message); }
    }

    public Actor resolve(Authentication authentication, Long onBehalfOfDiscordUserId) {
        if (authentication == null) {
            throw new ActorResolutionException("No authentication present");
        }
        if (isBot(authentication)) {
            String botId = botId(authentication);
            if (botId == null) {
                throw new ActorResolutionException("Bot principal missing identifier");
            }
            if (onBehalfOfDiscordUserId != null) {
                return Actor.botOnBehalfOf(botId, onBehalfOfDiscordUserId);
            }
            return Actor.bot(botId);
        }
        Long discordUserId = discordUserId(authentication);
        if (discordUserId == null) {
            throw new ActorResolutionException("Unable to resolve Discord user id from principal");
        }
        return Actor.user(discordUserId);
    }

    public boolean isBot(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_BOT".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private String botId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof String s && !s.isBlank()) {
            return s;
        }
        String name = authentication.getName();
        return (name == null || name.isBlank()) ? null : name;
    }

    private Long discordUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String id = oauth2User.getAttribute("id");
            if (id == null) {
                return null;
            }
            try {
                return Long.parseLong(id);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        if (principal instanceof String principalId) {
            try {
                return Long.parseLong(principalId);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }
}
