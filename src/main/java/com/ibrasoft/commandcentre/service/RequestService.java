package com.ibrasoft.commandcentre.service;

import com.ibrasoft.commandcentre.audit.Actor;
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
    public Request createRequest(Request request, Actor actor) {
        if (request.getRequesterID() == null && actor.kind() == Actor.Kind.USER) {
            request.setRequesterID(actor.discordUserId());
        }

        Request savedRequest = requestRepository.save(request);
        auditEventService.logRequestEvent("CREATE", savedRequest.getChannelID(),
            "Request created: " + savedRequest.getTitle(), actor);
        return savedRequest;
    }

    @Transactional
    public Request updateRequest(Long channelId, Request requestDetails, Actor actor) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));

        request.setRequesterDepartmentID(requestDetails.getRequesterDepartmentID());
        request.setAssignedToID(requestDetails.getAssignedToID());

        request.setTitle(requestDetails.getTitle());
        request.setDescription(requestDetails.getDescription());
        request.setStatus(requestDetails.getStatus());
        request.setPostingDate(requestDetails.getPostingDate());
        request.setRoom(requestDetails.getRoom());
        request.setSignupUrl(requestDetails.getSignupUrl());

        Request updatedRequest = requestRepository.save(request);
        auditEventService.logRequestEvent("UPDATE", updatedRequest.getChannelID(),
            "Request updated: " + updatedRequest.getTitle(), actor);
        return updatedRequest;
    }

    @Transactional
    public void deleteRequest(Long channelId, Actor actor) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));

        auditEventService.logRequestEvent("DELETE", channelId,
            "Request deleted: " + request.getTitle(), actor);
        requestRepository.deleteById(channelId);
    }

    @Transactional
    public Request assignRequest(Long channelId, Long assignedToId, Actor actor) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));

        Long previousAssignee = request.getAssignedToID();
        request.setAssignedToID(assignedToId);

        Request updatedRequest = requestRepository.save(request);
        auditEventService.logRequestEvent("ASSIGN", channelId,
            String.format("Request assigned from %s to %s", previousAssignee, assignedToId), actor);
        return updatedRequest;
    }

    @Transactional
    public Request setRequestStatus(Long channelId, RequestStatus status, Actor actor) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));

        RequestStatus previousStatus = request.getStatus();
        request.setStatus(status);

        Request updatedRequest = requestRepository.save(request);
        auditEventService.logRequestEvent("STATUS_CHANGE", channelId,
            String.format("Status changed from %s to %s", previousStatus.getDisplayName(), status.getDisplayName()), actor);
        return updatedRequest;
    }

    @Transactional
    public Request advanceRequestToNextStatus(Long channelId, Actor actor) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));

        RequestStatus currentStatus = request.getStatus();

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
        auditEventService.logRequestEvent("STATUS_ADVANCE", channelId,
            String.format("Status advanced from %s to %s", currentStatus.getDisplayName(), nextStatus.getDisplayName()), actor);
        return updatedRequest;
    }

    @Transactional
    public Request updateRequesterDepartment(Long channelId, Long requesterDepartmentID, Actor actor) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));

        Long previousDepartment = request.getRequesterDepartmentID();
        request.setRequesterDepartmentID(requesterDepartmentID);

        Request updatedRequest = requestRepository.save(request);
        auditEventService.logRequestEvent("DEPARTMENT_UPDATE", channelId,
            String.format("Requester department changed from %s to %s", previousDepartment, requesterDepartmentID), actor);
        return updatedRequest;
    }

    @Transactional
    public Request updateRequester(Long channelId, Long requesterID, Actor actor) {
        Request request = requestRepository.findById(channelId)
            .orElseThrow(() -> new RuntimeException("Request not found with channelId: " + channelId));

        Long previousRequester = request.getRequesterID();
        request.setRequesterID(requesterID);

        Request updatedRequest = requestRepository.save(request);
        auditEventService.logRequestEvent("REQUESTER_UPDATE", channelId,
            String.format("Requester changed from %s to %s", previousRequester, requesterID), actor);
        return updatedRequest;
    }

    @Transactional
    public List<DepartmentCount> getRequestCountsByDepartment() {
        return requestRepository.countByDepartment();
    }

    public List<Request> getContentCreatorWorkload() {
        CycleService.CycleInfo currentCycle = cycleService.getCurrentDevelopmentCycle();

        return requestRepository.findAll().stream()
            .filter(request -> request.getRequestType() == RequestType.REEL)
            .filter(request -> request.getPostingDate() != null)
            .filter(request -> isInCyclePostingPeriod(request.getPostingDate(), currentCycle))
            .sorted(Comparator.comparing(Request::getPostingDate))
            .collect(Collectors.toList());
    }

    public List<Request> getGraphicDesignerWorkload() {
        CycleService.CycleInfo currentCycle = cycleService.getCurrentDevelopmentCycle();

        return requestRepository.findAll().stream()
            .filter(request -> request.getRequestType() == RequestType.POST)
            .filter(request -> request.getPostingDate() != null)
            .filter(request -> isInCyclePostingPeriod(request.getPostingDate(), currentCycle))
            .sorted(Comparator.comparing(Request::getPostingDate))
            .collect(Collectors.toList());
    }

    public List<Request> getSocialMediaManagerWorkload() {
        CycleService.CycleInfo currentPostingCycle = cycleService.getCurrentPostingCycle();

        return requestRepository.findAll().stream()
            .filter(request -> request.getPostingDate() != null)
            .filter(request -> isInCyclePostingPeriod(request.getPostingDate(), currentPostingCycle))
            .sorted(Comparator.comparing(Request::getPostingDate))
            .collect(Collectors.toList());
    }

    private boolean isInCyclePostingPeriod(LocalDate postingDate, CycleService.CycleInfo cycle) {
        return !postingDate.isBefore(cycle.getPostingStart())
            && !postingDate.isAfter(cycle.getPostingEnd());
    }
}
