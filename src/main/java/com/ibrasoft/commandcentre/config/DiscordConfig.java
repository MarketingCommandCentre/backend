package com.ibrasoft.commandcentre.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "discord.required.guild")
@Data
public class DiscordConfig {
    private String id;
}
