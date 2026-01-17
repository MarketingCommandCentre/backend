package com.ibrasoft.commandcentre.service;

import com.ibrasoft.commandcentre.model.DepartmentCount;
import com.ibrasoft.commandcentre.model.Request;
import com.ibrasoft.commandcentre.model.RequestStatus;
import com.ibrasoft.commandcentre.model.RequestType;
import com.ibrasoft.commandcentre.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {
    
    private final RequestRepository requestRepository;
    private final AuditEventService auditEventService;
    private final CycleService cycleService;
    
    public List<Request> getAllRequests() {
        return requestRepository.findAll().stream()
            .sorted(Comparator.comparing(Request::getPostingDate, 
                Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }
    
    public Optional<Request> getRequestByChannelId(Long channelId) {
        return requestRepository.findById(channelId);
    }
    
    public List<Request> getRequestsByStatus(RequestStatus status) {
        return requestRepository.findByStatus(status);
    }
    
    public List<Request> getRequestsByRequester(Long requesterID) {
        return requestRepository.findByRequesterID(requesterID);
    }
    
    public List<Request> getRequestsByAssignedTo(Long assignedToID) {
        return requestRepository.findByAssignedToID(assignedToID);
    }
    
    @Transactional
    public Request createRequest(Request request, Long authenticatedUserId) {
        // Optionally verify the requester matches the authenticated user
        // or automatically set it if not provided
        if (request.getRequesterID() == null) {
            request.setRequesterID(authenticatedUserId);
        }
        
        Request savedRequest = requestRepository.save(request);
        auditEventService.logEvent("CREATE", "Request", savedRequest.getChannelID(), 
            "Request created: " + savedRequest.getTitle(), String.valueOf(authenticatedUserId));
        return savedRequest;
    }
    
    @Transactional
    public Request updateRequest(Long channelId, Request requestDetails, Long authenticatedUserId) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));
        
        // Update Discord-related fields
        request.setChannelID(requestDetails.getChannelID());
        // request.setRequesterID(requestDetails.getRequesterID());
        request.setRequesterDepartmentID(requestDetails.getRequesterDepartmentID());
        request.setAssignedToID(requestDetails.getAssignedToID());
        
        // Update marketing request fields
        request.setTitle(requestDetails.getTitle());
        request.setDescription(requestDetails.getDescription());
        request.setStatus(requestDetails.getStatus());
        request.setPostingDate(requestDetails.getPostingDate());
        request.setRoom(requestDetails.getRoom());
        request.setSignupUrl(requestDetails.getSignupUrl());
        
        Request updatedRequest = requestRepository.save(request);
        auditEventService.logEvent("UPDATE", "Request", updatedRequest.getChannelID(), 
            "Request updated: " + updatedRequest.getTitle(), String.valueOf(authenticatedUserId));
        return updatedRequest;
    }
    
    @Transactional
    public void deleteRequest(Long channelId, Long authenticatedUserId) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));
        
        auditEventService.logEvent("DELETE", "Request", channelId, 
            "Request deleted: " + request.getTitle(), String.valueOf(authenticatedUserId));
        requestRepository.deleteById(channelId);
    }
    
    @Transactional
    public Request assignRequest(Long channelId, Long assignedToId, Long authenticatedUserId) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));
        
        Long previousAssignee = request.getAssignedToID();
        request.setAssignedToID(assignedToId);
        
        Request updatedRequest = requestRepository.save(request);
        auditEventService.logEvent("ASSIGN", "Request", channelId, 
            String.format("Request assigned from %s to %s", previousAssignee, assignedToId), 
            String.valueOf(authenticatedUserId));
        return updatedRequest;
    }
    
    @Transactional
    public Request setRequestStatus(Long channelId, RequestStatus status, Long authenticatedUserId) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));
        
        RequestStatus previousStatus = request.getStatus();
        request.setStatus(status);
        
        Request updatedRequest = requestRepository.save(request);
        auditEventService.logEvent("STATUS_CHANGE", "Request", channelId, 
            String.format("Status changed from %s to %s", previousStatus.getDisplayName(), status.getDisplayName()), 
            String.valueOf(authenticatedUserId));
        return updatedRequest;
    }
    
    @Transactional
    public Request advanceRequestToNextStatus(Long channelId, Long authenticatedUserId) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));
        
        RequestStatus currentStatus = request.getStatus();
        
        // Define the status progression
        RequestStatus nextStatus;
        switch (currentStatus) {
            case IN_QUEUE:
                nextStatus = RequestStatus.IN_PROGRESS;
                break;
            case IN_PROGRESS:
                nextStatus = RequestStatus.AWAITING_POSTING;
                break;
            case AWAITING_POSTING:
                nextStatus = RequestStatus.DONE;
                break;
            case DONE:
                throw new IllegalStateException("Request is already in DONE state and cannot be advanced further");
            case BLOCKED:
                throw new IllegalStateException("Request is BLOCKED and cannot be advanced automatically");
            default:
                throw new IllegalStateException("Unknown status: " + currentStatus);
        }
        
        request.setStatus(nextStatus);
        Request updatedRequest = requestRepository.save(request);
        auditEventService.logEvent("STATUS_ADVANCE", "Request", channelId, 
            String.format("Status advanced from %s to %s", currentStatus.getDisplayName(), nextStatus.getDisplayName()), 
            String.valueOf(authenticatedUserId));
        return updatedRequest;
    }
    
    @Transactional
    public Request updateRequesterDepartment(Long channelId, Long requesterDepartmentID, Long authenticatedUserId) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));
        
        Long previousDepartment = request.getRequesterDepartmentID();
        request.setRequesterDepartmentID(requesterDepartmentID);
        
        Request updatedRequest = requestRepository.save(request);
        auditEventService.logEvent("DEPARTMENT_UPDATE", "Request", channelId, 
            String.format("Requester department changed from %s to %s", previousDepartment, requesterDepartmentID), 
            String.valueOf(authenticatedUserId));
        return updatedRequest;
    }
    
    @Transactional
    public Request updateRequester(Long channelId, Long requesterID, Long authenticatedUserId) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));
        
        Long previousRequester = request.getRequesterID();
        request.setRequesterID(requesterID);
        
        Request updatedRequest = requestRepository.save(request);
        auditEventService.logEvent("REQUESTER_UPDATE", "Request", channelId, 
            String.format("Requester changed from %s to %s", previousRequester, requesterID), 
            String.valueOf(authenticatedUserId));
        return updatedRequest;
    }
    
    @Transactional
    public List<DepartmentCount> getRequestCountsByDepartment() {
        return requestRepository.countByDepartment();
    }

    // ========== Workload Aggregation Methods ==========
    
    /**
     * Get workload for content creators (REEL type requests in current development cycle)
     */
    public List<Request> getContentCreatorWorkload() {
        CycleService.CycleInfo currentCycle = cycleService.getCurrentDevelopmentCycle();
        
        return requestRepository.findAll().stream()
            .filter(request -> request.getRequestType() == RequestType.REEL)
            .filter(request -> request.getPostingDate() != null)
            .filter(request -> isInCyclePostingPeriod(request.getPostingDate(), currentCycle))
            .sorted(Comparator.comparing(Request::getPostingDate))
            .collect(Collectors.toList());
    }
    
    /**
     * Get workload for graphic designers (POST type requests in current development cycle)
     */
    public List<Request> getGraphicDesignerWorkload() {
        CycleService.CycleInfo currentCycle = cycleService.getCurrentDevelopmentCycle();
        
        return requestRepository.findAll().stream()
            .filter(request -> request.getRequestType() == RequestType.POST)
            .filter(request -> request.getPostingDate() != null)
            .filter(request -> isInCyclePostingPeriod(request.getPostingDate(), currentCycle))
            .sorted(Comparator.comparing(Request::getPostingDate))
            .collect(Collectors.toList());
    }
    
    /**
     * Get workload for social media managers (all requests in current posting cycle)
     */
    public List<Request> getSocialMediaManagerWorkload() {
        CycleService.CycleInfo currentPostingCycle = cycleService.getCurrentPostingCycle();
        
        return requestRepository.findAll().stream()
            .filter(request -> request.getPostingDate() != null)
            .filter(request -> isInCyclePostingPeriod(request.getPostingDate(), currentPostingCycle))
            .sorted(Comparator.comparing(Request::getPostingDate))
            .collect(Collectors.toList());
    }
    
    /**
     * Helper method to check if a posting date falls within a cycle's posting period
     */
    private boolean isInCyclePostingPeriod(LocalDate postingDate, CycleService.CycleInfo cycle) {
        return !postingDate.isBefore(cycle.getPostingStart()) 
            && !postingDate.isAfter(cycle.getPostingEnd());
    }
}
