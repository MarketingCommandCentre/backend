package com.ibrasoft.commandcentre.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String eventType;

    @NotBlank
    @Column(nullable = false)
    private String entityType;

    @NotNull
    private Long entityId;

    @Size(max = 2000)
    @Column(length = 2000)
    private String eventDetails;

    @NotBlank
    private String performedBy;

    /**
     * Optional structured detail as a JSON string (e.g. {@code {"assigneeIds":["123"],"via":"role"}}).
     * Complements the human-readable {@link #eventDetails} summary. Nullable; snowflake IDs are stored
     * as strings to avoid precision loss on the JS frontend.
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime eventTimestamp;

    @PrePersist
    protected void onCreate() {
        eventTimestamp = LocalDateTime.now();
    }
}
