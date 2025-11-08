package com.ibrasoft.commandcentre.model;

public enum RequestType {
    POST("post"),
    REEL("reel");

    private final String displayName;

    RequestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RequestType fromDisplayName(String displayName) {
        for (RequestType type : RequestType.values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown request type: " + displayName);
    }
}
