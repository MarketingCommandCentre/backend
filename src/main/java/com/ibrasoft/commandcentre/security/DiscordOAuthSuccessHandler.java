package com.ibrasoft.commandcentre.security;

import com.ibrasoft.commandcentre.model.Role;
import com.ibrasoft.commandcentre.service.DiscordService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordOAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final DiscordService discordService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String userId = oauth2User.getAttribute("id");
            if (userId != null) {
                boolean allowed = discordService.isUserInRequiredGuild(userId);
                if (!allowed) {
                    log.warn("OAuth login denied for user {} not in required guild", userId);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "You must be a member of the required Discord server to access this API");
                    return;
                }
                String token = jwtService.generateUserToken(userId, List.of(Role.ROLE_USER));
                String redirect = UriComponentsBuilder.fromUriString(frontendUrl)
                    .queryParam("token", token)
                    .build()
                    .toUriString();
                response.sendRedirect(redirect);
                return;
            }
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
    }
}
