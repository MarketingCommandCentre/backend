package com.ibrasoft.commandcentre.model;

import jakarta.persistence.*;
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
    
    @Column(nullable = true)
    private String eventType;
    
    @Column(nullable = true)
    private String entityType;
    
    private Long entityId;
    
    @Column(length = 2000)
    private String eventDetails;
    
    private String performedBy;
    
    @Column(nullable = true)
    private LocalDateTime eventTimestamp;
    
    @PrePersist
    protected void onCreate() {
        eventTimestamp = LocalDateTime.now();
    }
}
