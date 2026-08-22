package com.servicedesk.repository;

import com.servicedesk.entity.Priority;
import com.servicedesk.entity.Status;
import com.servicedesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(Status status);

    List<Ticket> findByPriority(Priority priority);
}
