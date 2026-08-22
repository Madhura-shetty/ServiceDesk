package com.servicedesk.dto;

import jakarta.validation.constraints.NotNull;

public class TicketAssignRequest {

    @NotNull(message = "agentId is required")
    private Long agentId;

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }
}
