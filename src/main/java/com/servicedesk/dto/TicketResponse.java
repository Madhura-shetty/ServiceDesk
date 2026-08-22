package com.servicedesk.dto;

import com.servicedesk.entity.Priority;
import com.servicedesk.entity.SlaStatus;
import com.servicedesk.entity.Status;
import com.servicedesk.entity.Ticket;
import java.time.LocalDateTime;

public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private Priority priority;
    private Status status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime resolutionDeadline;
    private SlaStatus slaStatus;

    private UserResponse createdBy;
    private UserResponse assignedAgent;

    public TicketResponse() {
    }

    public TicketResponse(Ticket ticket, SlaStatus slaStatus) {
        this.id = ticket.getId();
        this.title = ticket.getTitle();
        this.description = ticket.getDescription();
        this.category = ticket.getCategory();
        this.priority = ticket.getPriority();
        this.status = ticket.getStatus();
        this.createdDate = ticket.getCreatedDate();
        this.updatedDate = ticket.getUpdatedDate();
        this.resolutionDeadline = ticket.getResolutionDeadline();
        this.slaStatus = slaStatus;
        this.createdBy = ticket.getCreatedBy() != null ? new UserResponse(ticket.getCreatedBy()) : null;
        this.assignedAgent = ticket.getAssignedAgent() != null ? new UserResponse(ticket.getAssignedAgent()) : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public LocalDateTime getResolutionDeadline() {
        return resolutionDeadline;
    }

    public void setResolutionDeadline(LocalDateTime resolutionDeadline) {
        this.resolutionDeadline = resolutionDeadline;
    }

    public SlaStatus getSlaStatus() {
        return slaStatus;
    }

    public void setSlaStatus(SlaStatus slaStatus) {
        this.slaStatus = slaStatus;
    }

    public UserResponse getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserResponse createdBy) {
        this.createdBy = createdBy;
    }

    public UserResponse getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(UserResponse assignedAgent) {
        this.assignedAgent = assignedAgent;
    }
}
