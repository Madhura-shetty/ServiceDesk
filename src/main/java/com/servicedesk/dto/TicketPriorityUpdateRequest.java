package com.servicedesk.dto;

import com.servicedesk.entity.Priority;
import jakarta.validation.constraints.NotNull;

public class TicketPriorityUpdateRequest {

    @NotNull(message = "Priority is required")
    private Priority priority;

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
