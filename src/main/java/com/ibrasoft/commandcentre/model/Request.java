package com.ibrasoft.commandcentre.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @NotNull
    private Long channelID;

    /**
     * DISCORD RELATED FIELDS
     *
     * This includes stuff like channel IDs, message IDs, guild IDs, etc.
     */

    private Long requesterID;

    private Long requesterDepartmentID;

    private Long assignedToID;

    @Column(name = "additionalAsigneeID")
    private Long additionalAssigneeID;

    @NotNull
    @Column(nullable = false)
    private Long mainMessageID;

    /**
     * MARKETING REQUEST RELATED FIELDS
     *
     * Everything else related to the request, but not directly related to Discord
     */

    @Size(max = 255)
    private String title;

    @Size(max = 4000)
    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDate postingDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Size(max = 255)
    @Column(length = 255, nullable = true)
    private String room;

    @Size(max = 255)
    @Column(length = 255, nullable = true)
    private String signupUrl;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
