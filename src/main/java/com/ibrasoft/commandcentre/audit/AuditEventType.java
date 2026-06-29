package com.ibrasoft.commandcentre.audit;

import java.util.Optional;

/**
 * Canonical set of audit event types.
 *
 * <p>The enum name is what gets persisted to {@code audit_events.event_type} (via
 * {@link #name()}), so existing rows and the dashboard's keyword-based styling keep working.
 * The enum exists to give type-safety at the call sites and to let the write endpoint reject
 * unknown event types.
 */
public enum AuditEventType {

    // Request lifecycle (logged as a side-effect of request mutations).
    CREATE,
    UPDATE,
    DELETE,
    ASSIGN,
    UNASSIGN,
    STATUS_CHANGE,
    STATUS_ADVANCE,
    DEPARTMENT_UPDATE,
    REQUESTER_UPDATE,

    // Discord-only actions, logged directly by the bot via POST /api/audit-events.
    ASSIGNEE_ADD,
    ASSIGNEE_REMOVE,
    REQUEST_SPLIT,
    CHANNEL_RENAME,
    PERMISSIONS_SYNC,
    CATEGORY_MOVE;

    /** Case-insensitive lookup that never throws; empty when {@code value} is unknown. */
    public static Optional<AuditEventType> fromString(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(AuditEventType.valueOf(value.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
