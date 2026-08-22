package com.servicedesk.dto;

import com.servicedesk.entity.Status;
import jakarta.validation.constraints.NotNull;

public class TicketStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
