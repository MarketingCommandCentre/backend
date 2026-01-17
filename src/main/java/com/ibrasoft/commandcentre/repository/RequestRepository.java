package com.ibrasoft.commandcentre.repository;

import com.ibrasoft.commandcentre.model.DepartmentCount;
import com.ibrasoft.commandcentre.model.Request;
import com.ibrasoft.commandcentre.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    
    List<Request> findByStatus(RequestStatus status);
    
    List<Request> findByRequesterID(Long requesterID);
    
    List<Request> findByAssignedToID(Long assignedToID);

    @Query(value = "SELECT requester_departmentid, COUNT(*) AS total_requests FROM requests GROUP BY requester_departmentid ORDER BY total_requests DESC", nativeQuery = true)
    List<DepartmentCount> countByDepartment();
}
