package com.ibrasoft.commandcentre.controller.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Request body for {@code POST /api/audit-events}.
 *
 * <p>{@code metadata} is captured as raw JSON ({@link JsonNode}) so callers can send arbitrary
 * structured detail; the controller serializes it to a string for storage. {@code entityType}
 * defaults to {@code "Request"} when omitted.
 */
public record AuditEventRequest(
    String eventType,
    String entityType,
    Long entityId,
    String eventDetails,
    JsonNode metadata
) {}
