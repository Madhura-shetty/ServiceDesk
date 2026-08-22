package com.servicedesk.controller;

import com.servicedesk.dto.*;
import com.servicedesk.entity.Priority;
import com.servicedesk.entity.Status;
import com.servicedesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        TicketResponse created = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketResponse>> getTicketsByStatus(@PathVariable Status status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TicketResponse>> getTicketsByPriority(@PathVariable Priority priority) {
        return ResponseEntity.ok(ticketService.getTicketsByPriority(priority));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody TicketStatusUpdateRequest request) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request.getStatus()));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignAgent(@PathVariable Long id,
                                                        @Valid @RequestBody TicketAssignRequest request) {
        return ResponseEntity.ok(ticketService.assignAgent(id, request.getAgentId()));
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<TicketResponse> updatePriority(@PathVariable Long id,
                                                           @Valid @RequestBody TicketPriorityUpdateRequest request) {
        return ResponseEntity.ok(ticketService.updatePriority(id, request.getPriority()));
    }

    @GetMapping("/stats")
    public ResponseEntity<TicketStatsResponse> getStats() {
        return ResponseEntity.ok(ticketService.getStats());
    }
}
