package com.ibrasoft.commandcentre.repository;

import com.ibrasoft.commandcentre.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    
    List<AuditEvent> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    List<AuditEvent> findByEventType(String eventType);
    
    List<AuditEvent> findByPerformedBy(String performedBy);
    
    List<AuditEvent> findByEventTimestampBetween(LocalDateTime start, LocalDateTime end);
}
