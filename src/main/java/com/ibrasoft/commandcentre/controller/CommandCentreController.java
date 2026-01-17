package com.ibrasoft.commandcentre.controller;

import com.ibrasoft.commandcentre.model.AuditEvent;
import com.ibrasoft.commandcentre.model.DepartmentCount;
import com.ibrasoft.commandcentre.model.Request;
import com.ibrasoft.commandcentre.model.RequestStatus;
import com.ibrasoft.commandcentre.service.AuditEventService;
import com.ibrasoft.commandcentre.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommandCentreController {
    
    private final RequestService requestService;
    private final AuditEventService auditEventService;
    
    // ========== Request Endpoints ==========
    
    @GetMapping("/requests")
    public ResponseEntity<List<Request>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }
    
    @GetMapping("/requests/channel/{channelId}")
    public ResponseEntity<Request> getRequestByChannelId(@PathVariable Long channelId) {
        return requestService.getRequestByChannelId(channelId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/requests/status/{status}")
    public ResponseEntity<List<Request>> getRequestsByStatus(@PathVariable RequestStatus status) {
        return ResponseEntity.ok(requestService.getRequestsByStatus(status));
    }
    
    @GetMapping("/requests/requester/{requesterId}")
    public ResponseEntity<List<Request>> getRequestsByRequester(@PathVariable Long requesterId) {
        return ResponseEntity.ok(requestService.getRequestsByRequester(requesterId));
    }
    
    @GetMapping("/requests/assigned/{assignedToId}")
    public ResponseEntity<List<Request>> getRequestsByAssignedTo(@PathVariable Long assignedToId) {
        return ResponseEntity.ok(requestService.getRequestsByAssignedTo(assignedToId));
    }
    
    @GetMapping("/requests/my-requests")
    public ResponseEntity<List<Request>> getMyRequests(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long discordUserId = Long.parseLong(principal.getAttribute("id"));
        return ResponseEntity.ok(requestService.getRequestsByRequester(discordUserId));
    }
    
    @PostMapping("/requests")
    public ResponseEntity<Request> createRequest(
            @RequestBody Request request,
            @AuthenticationPrincipal OAuth2User principal) {
        // If authenticated as bot, use requesterID from request body
        if (principal == null) {
            // Check if this is a bot request
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals("discord-bot")) {
                // Bot must provide requesterID in the request body
                if (request.getRequesterID() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                Request createdRequest = requestService.createRequest(request, request.getRequesterID());
                return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long discordUserId = Long.parseLong(principal.getAttribute("id"));
        Request createdRequest = requestService.createRequest(request, discordUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }
    
    @PutMapping("/requests/channel/{channelId}")
    public ResponseEntity<Request> updateRequest(
            @PathVariable Long channelId,
            @RequestBody Request request,
            @AuthenticationPrincipal OAuth2User principal) {
        // If authenticated as bot, use a system identifier
        if (principal == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals("discord-bot")) {
                try {
                    Request updatedRequest = requestService.updateRequest(channelId, request, 0L); // 0 = bot
                    return ResponseEntity.ok(updatedRequest);
                } catch (RuntimeException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long discordUserId = Long.parseLong(principal.getAttribute("id"));
            Request updatedRequest = requestService.updateRequest(channelId, request, discordUserId);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/requests/channel/{channelId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Long channelId,
            @AuthenticationPrincipal OAuth2User principal) {
        // If authenticated as bot, use a system identifier
        if (principal == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals("discord-bot")) {
                try {
                    requestService.deleteRequest(channelId, 0L); // 0 = bot
                    return ResponseEntity.noContent().build();
                } catch (RuntimeException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long discordUserId = Long.parseLong(principal.getAttribute("id"));
            requestService.deleteRequest(channelId, discordUserId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/requests/channel/{channelId}/assign/{assignedToId}")
    public ResponseEntity<Request> assignRequest(
            @PathVariable Long channelId,
            @PathVariable Long assignedToId,
            @AuthenticationPrincipal OAuth2User principal) {
        // If authenticated as bot, use a system identifier
        if (principal == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals("discord-bot")) {
                try {
                    Request updatedRequest = requestService.assignRequest(channelId, assignedToId, 0L); // 0 = bot
                    return ResponseEntity.ok(updatedRequest);
                } catch (RuntimeException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long discordUserId = Long.parseLong(principal.getAttribute("id"));
            Request updatedRequest = requestService.assignRequest(channelId, assignedToId, discordUserId);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/requests/channel/{channelId}/status/{status}")
    public ResponseEntity<?> setRequestStatus(
            @PathVariable Long channelId,
            @PathVariable String status,
            @AuthenticationPrincipal OAuth2User principal) {
        // If authenticated as bot, use a system identifier
        if (principal == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals("discord-bot")) {
                try {
                    RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
                    Request updatedRequest = requestService.setRequestStatus(channelId, requestStatus, 0L); // 0 = bot
                    return ResponseEntity.ok(updatedRequest);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Invalid status: " + status);
                } catch (RuntimeException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long discordUserId = Long.parseLong(principal.getAttribute("id"));
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
            Request updatedRequest = requestService.setRequestStatus(channelId, requestStatus, discordUserId);
            return ResponseEntity.ok(updatedRequest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/requests/channel/{channelId}/advance")
    public ResponseEntity<?> advanceRequestToNextStatus(
            @PathVariable Long channelId,
            @AuthenticationPrincipal OAuth2User principal) {
        // If authenticated as bot, use a system identifier
        if (principal == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals("discord-bot")) {
                try {
                    Request updatedRequest = requestService.advanceRequestToNextStatus(channelId, 0L); // 0 = bot
                    return ResponseEntity.ok(updatedRequest);
                } catch (IllegalStateException e) {
                    return ResponseEntity.badRequest().body(e.getMessage());
                } catch (RuntimeException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long discordUserId = Long.parseLong(principal.getAttribute("id"));
            Request updatedRequest = requestService.advanceRequestToNextStatus(channelId, discordUserId);
            return ResponseEntity.ok(updatedRequest);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/requests/channel/{channelId}/department/{departmentId}")
    public ResponseEntity<Request> updateRequesterDepartment(
            @PathVariable Long channelId,
            @PathVariable Long departmentId,
            @AuthenticationPrincipal OAuth2User principal) {
        // If authenticated as bot, use a system identifier
        if (principal == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals("discord-bot")) {
                try {
                    Request updatedRequest = requestService.updateRequesterDepartment(channelId, departmentId, 0L); // 0 = bot
                    return ResponseEntity.ok(updatedRequest);
                } catch (RuntimeException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long discordUserId = Long.parseLong(principal.getAttribute("id"));
            Request updatedRequest = requestService.updateRequesterDepartment(channelId, departmentId, discordUserId);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/requests/channel/{channelId}/requester/{requesterId}")
    public ResponseEntity<Request> updateRequester(
            @PathVariable Long channelId,
            @PathVariable Long requesterId,
            @AuthenticationPrincipal OAuth2User principal) {
        // If authenticated as bot, use a system identifier
        if (principal == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals("discord-bot")) {
                try {
                    Request updatedRequest = requestService.updateRequester(channelId, requesterId, 0L); // 0 = bot
                    return ResponseEntity.ok(updatedRequest);
                } catch (RuntimeException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long discordUserId = Long.parseLong(principal.getAttribute("id"));
            Request updatedRequest = requestService.updateRequester(channelId, requesterId, discordUserId);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/requests/countByDepartment")
    public ResponseEntity<List<DepartmentCount>> countRequestsByDepartment(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals("discord-bot")) {
                try {
                    List<DepartmentCount> counts = requestService.getRequestCountsByDepartment();
                    return ResponseEntity.ok(counts);
                } catch (RuntimeException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            List<DepartmentCount> counts = requestService.getRequestCountsByDepartment();
            return ResponseEntity.ok(counts);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    // ========== Audit Event Endpoints ==========
    
    @GetMapping("/audit-events")
    public ResponseEntity<List<AuditEvent>> getAllAuditEvents() {
        return ResponseEntity.ok(auditEventService.getAllAuditEvents());
    }
    
    @GetMapping("/audit-events/{id}")
    public ResponseEntity<AuditEvent> getAuditEventById(@PathVariable Long id) {
        return auditEventService.getAuditEventById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/audit-events/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditEvent>> getAuditEventsByEntity(
            @PathVariable String entityType, 
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditEventService.getAuditEventsByEntityTypeAndId(entityType, entityId));
    }
    
    @GetMapping("/audit-events/type/{eventType}")
    public ResponseEntity<List<AuditEvent>> getAuditEventsByType(@PathVariable String eventType) {
        return ResponseEntity.ok(auditEventService.getAuditEventsByEventType(eventType));
    }
    
    @GetMapping("/audit-events/user/{performedBy}")
    public ResponseEntity<List<AuditEvent>> getAuditEventsByUser(@PathVariable String performedBy) {
        return ResponseEntity.ok(auditEventService.getAuditEventsByPerformedBy(performedBy));
    }
    
    @GetMapping("/audit-events/daterange")
    public ResponseEntity<List<AuditEvent>> getAuditEventsByDateRange(
            @RequestParam String start, 
            @RequestParam String end) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        return ResponseEntity.ok(auditEventService.getAuditEventsByDateRange(startDate, endDate));
    }
    
    @PostMapping("/audit-events")
    public ResponseEntity<AuditEvent> createAuditEvent(
            @RequestParam String eventType,
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam String eventDetails,
            @RequestParam String performedBy) {
        AuditEvent auditEvent = auditEventService.logEvent(
            eventType, entityType, entityId, eventDetails, performedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(auditEvent);
    }
}
