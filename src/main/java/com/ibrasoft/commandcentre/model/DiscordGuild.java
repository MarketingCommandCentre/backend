package com.ibrasoft.commandcentre.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscordGuild {
    private String id;
    private String name;
    private String icon;
    private Boolean owner;
    // Discord returns permissions as a string (bitwise value), and it may exceed 32-bit integer range
    private String permissions;
}
