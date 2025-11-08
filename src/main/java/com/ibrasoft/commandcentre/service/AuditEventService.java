package com.ibrasoft.commandcentre.service;

import com.ibrasoft.commandcentre.model.AuditEvent;
import com.ibrasoft.commandcentre.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditEventService {
    
    private final AuditEventRepository auditEventRepository;
    
    public List<AuditEvent> getAllAuditEvents() {
        return auditEventRepository.findAll();
    }
    
    public Optional<AuditEvent> getAuditEventById(Long id) {
        return auditEventRepository.findById(id);
    }
    
    public List<AuditEvent> getAuditEventsByEntityTypeAndId(String entityType, Long entityId) {
        return auditEventRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    public List<AuditEvent> getAuditEventsByEventType(String eventType) {
        return auditEventRepository.findByEventType(eventType);
    }
    
    public List<AuditEvent> getAuditEventsByPerformedBy(String performedBy) {
        return auditEventRepository.findByPerformedBy(performedBy);
    }
    
    public List<AuditEvent> getAuditEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditEventRepository.findByEventTimestampBetween(start, end);
    }
    
    @Transactional
    public AuditEvent logEvent(String eventType, String entityType, Long entityId, 
                                String eventDetails, String performedBy) {
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventType(eventType);
        auditEvent.setEntityType(entityType);
        auditEvent.setEntityId(entityId);
        auditEvent.setEventDetails(eventDetails);
        auditEvent.setPerformedBy(performedBy);
        return auditEventRepository.save(auditEvent);
    }
}
