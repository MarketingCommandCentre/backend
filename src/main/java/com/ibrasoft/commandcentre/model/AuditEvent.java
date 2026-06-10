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

    @Column(nullable = false)
    private LocalDateTime eventTimestamp;

    @PrePersist
    protected void onCreate() {
        eventTimestamp = LocalDateTime.now();
    }
}
