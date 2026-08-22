package com.servicedesk.service;

import com.servicedesk.dto.*;
import com.servicedesk.entity.*;
import com.servicedesk.exception.InvalidRequestException;
import com.servicedesk.exception.ResourceNotFoundException;
import com.servicedesk.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;

    // Defines which status transitions are logically allowed.
    private static final Map<Status, EnumSet<Status>> ALLOWED_TRANSITIONS = new EnumMap<>(Status.class);

    static {
        ALLOWED_TRANSITIONS.put(Status.OPEN, EnumSet.of(Status.IN_PROGRESS));
        ALLOWED_TRANSITIONS.put(Status.IN_PROGRESS, EnumSet.of(Status.RESOLVED, Status.OPEN));
        ALLOWED_TRANSITIONS.put(Status.RESOLVED, EnumSet.of(Status.CLOSED, Status.IN_PROGRESS));
        ALLOWED_TRANSITIONS.put(Status.CLOSED, EnumSet.noneOf(Status.class));
    }

    public TicketService(TicketRepository ticketRepository, UserService userService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
    }

    public TicketResponse createTicket(TicketCreateRequest request) {
        User creator = userService.findUserEntityById(request.getCreatedById());

        if (creator.getRole() != Role.EMPLOYEE) {
            throw new InvalidRequestException("Only an EMPLOYEE can create a ticket");
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());
        ticket.setStatus(Status.OPEN);
        ticket.setCreatedBy(creator);

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse getTicketById(Long id) {
        return toResponse(findTicketEntityById(id));
    }

    public List<TicketResponse> getTicketsByStatus(Status status) {
        return ticketRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getTicketsByPriority(Priority priority) {
        return ticketRepository.findByPriority(priority).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse updateStatus(Long ticketId, Status newStatus) {
        Ticket ticket = findTicketEntityById(ticketId);
        validateTransition(ticket.getStatus(), newStatus);

        if (newStatus == Status.IN_PROGRESS && ticket.getAssignedAgent() == null) {
            throw new InvalidRequestException("Ticket must be assigned to a support agent before moving to IN_PROGRESS");
        }

        ticket.setStatus(newStatus);
        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    public TicketResponse assignAgent(Long ticketId, Long agentId) {
        Ticket ticket = findTicketEntityById(ticketId);
        User agent = userService.findUserEntityById(agentId);

        if (agent.getRole() != Role.SUPPORT_AGENT) {
            throw new InvalidRequestException("Ticket can only be assigned to a SUPPORT_AGENT");
        }

        if (ticket.getStatus() == Status.CLOSED) {
            throw new InvalidRequestException("Cannot assign an agent to a CLOSED ticket");
        }

        ticket.setAssignedAgent(agent);

        // Logical workflow: assigning an agent moves an OPEN ticket to IN_PROGRESS.
        if (ticket.getStatus() == Status.OPEN) {
            ticket.setStatus(Status.IN_PROGRESS);
        }

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    public TicketResponse updatePriority(Long ticketId, Priority newPriority) {
        Ticket ticket = findTicketEntityById(ticketId);

        if (ticket.getStatus() == Status.CLOSED) {
            throw new InvalidRequestException("Cannot change priority of a CLOSED ticket");
        }

        ticket.setPriority(newPriority);
        // Recalculate the resolution deadline based on the original creation time and new priority.
        ticket.setResolutionDeadline(ticket.getCreatedDate().plusHours(Ticket.slaHoursFor(newPriority)));

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    public TicketStatsResponse getStats() {
        List<Ticket> all = ticketRepository.findAll();

        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        Map<String, Long> byPriority = all.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        long breached = all.stream().filter(t -> computeSlaStatus(t) == SlaStatus.BREACHED).count();
        long withinSla = all.size() - breached;

        return new TicketStatsResponse(all.size(), byStatus, byPriority, breached, withinSla);
    }

    public Ticket findTicketEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    private void validateTransition(Status current, Status target) {
        if (current == target) {
            return;
        }
        EnumSet<Status> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(Status.class));
        if (!allowed.contains(target)) {
            throw new InvalidRequestException(
                    "Invalid status transition from " + current + " to " + target);
        }
    }

    /**
     * SLA rule: compares "now" (for open/in-progress tickets) or the last update time
     * (for resolved/closed tickets) against the resolution deadline.
     */
    private SlaStatus computeSlaStatus(Ticket ticket) {
        LocalDateTime reference;
        if (ticket.getStatus() == Status.RESOLVED || ticket.getStatus() == Status.CLOSED) {
            reference = ticket.getUpdatedDate();
        } else {
            reference = LocalDateTime.now();
        }

        return reference.isAfter(ticket.getResolutionDeadline()) ? SlaStatus.BREACHED : SlaStatus.WITHIN_SLA;
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(ticket, computeSlaStatus(ticket));
    }
}
