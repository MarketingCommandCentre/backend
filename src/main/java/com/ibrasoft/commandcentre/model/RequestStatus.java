package com.ibrasoft.commandcentre.model;

public enum RequestStatus {
    IN_QUEUE("in queue"),
    IN_PROGRESS("in progress"),
    AWAITING_POSTING("awaiting posting"),
    DONE("done"),
    BLOCKED("blocked");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RequestStatus fromDisplayName(String displayName) {
        for (RequestStatus status : RequestStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + displayName);
    }
}
