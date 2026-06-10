package com.ibrasoft.commandcentre.audit;

public record Actor(Kind kind, Long discordUserId, String botId) {

    public enum Kind { USER, BOT, BOT_ON_BEHALF_OF }

    public static Actor user(Long discordUserId) {
        if (discordUserId == null) {
            throw new IllegalArgumentException("discordUserId required for USER actor");
        }
        return new Actor(Kind.USER, discordUserId, null);
    }

    public static Actor bot(String botId) {
        if (botId == null || botId.isBlank()) {
            throw new IllegalArgumentException("botId required for BOT actor");
        }
        return new Actor(Kind.BOT, null, botId);
    }

    public static Actor botOnBehalfOf(String botId, Long discordUserId) {
        if (botId == null || botId.isBlank()) {
            throw new IllegalArgumentException("botId required");
        }
        if (discordUserId == null) {
            throw new IllegalArgumentException("discordUserId required");
        }
        return new Actor(Kind.BOT_ON_BEHALF_OF, discordUserId, botId);
    }

    public String format() {
        return switch (kind) {
            case USER -> "user:" + discordUserId;
            case BOT -> "bot:" + botId;
            case BOT_ON_BEHALF_OF -> "bot:" + botId + ";on-behalf-of:" + discordUserId;
        };
    }
}
