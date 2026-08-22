package com.servicedesk.dto;

import java.util.Map;

public class TicketStatsResponse {

    private long totalTickets;
    private Map<String, Long> byStatus;
    private Map<String, Long> byPriority;
    private long breachedCount;
    private long withinSlaCount;

    public TicketStatsResponse() {
    }

    public TicketStatsResponse(long totalTickets, Map<String, Long> byStatus, Map<String, Long> byPriority,
                                long breachedCount, long withinSlaCount) {
        this.totalTickets = totalTickets;
        this.byStatus = byStatus;
        this.byPriority = byPriority;
        this.breachedCount = breachedCount;
        this.withinSlaCount = withinSlaCount;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Map<String, Long> getByStatus() {
        return byStatus;
    }

    public void setByStatus(Map<String, Long> byStatus) {
        this.byStatus = byStatus;
    }

    public Map<String, Long> getByPriority() {
        return byPriority;
    }

    public void setByPriority(Map<String, Long> byPriority) {
        this.byPriority = byPriority;
    }

    public long getBreachedCount() {
        return breachedCount;
    }

    public void setBreachedCount(long breachedCount) {
        this.breachedCount = breachedCount;
    }

    public long getWithinSlaCount() {
        return withinSlaCount;
    }

    public void setWithinSlaCount(long withinSlaCount) {
        this.withinSlaCount = withinSlaCount;
    }
}
