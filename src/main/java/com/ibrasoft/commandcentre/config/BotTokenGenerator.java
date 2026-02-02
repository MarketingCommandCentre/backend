package com.ibrasoft.commandcentre.config;

import com.ibrasoft.commandcentre.model.Role;
import com.ibrasoft.commandcentre.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generates a long-lived JWT for the Discord bot.
 * 
 * Run with: mvn spring-boot:run -Dspring-boot.run.profiles=generate-bot-token
 * Or:       ./mvnw spring-boot:run -Dspring-boot.run.profiles=generate-bot-token
 * 
 * The token will be printed to the console. Give it to the bot as configuration.
 */
@Component
@Profile("generate-bot-token")
@RequiredArgsConstructor
public class BotTokenGenerator implements CommandLineRunner {

    private final JwtService jwtService;

    @Override
    public void run(String... args) {
        String token = jwtService.generateBotToken("discord-bot", List.of(Role.ROLE_BOT));
        
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("BOT JWT TOKEN");
        System.out.println("=".repeat(80));
        System.out.println(token);
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("Give this token to the bot. It should use it as:");
        System.out.println("  Authorization: Bearer <token>");
        System.out.println();
        
        // Exit immediately after generating token
        System.exit(0);
    }
}
