package com.ibrasoft.commandcentre.repository;

import com.ibrasoft.commandcentre.model.Request;
import com.ibrasoft.commandcentre.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    
    List<Request> findByStatus(RequestStatus status);
    
    List<Request> findByRequesterID(Long requesterID);
    
    List<Request> findByAssignedToID(Long assignedToID);
}
