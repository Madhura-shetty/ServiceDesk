package com.servicedesk.service;

import com.servicedesk.dto.CommentCreateRequest;
import com.servicedesk.dto.CommentResponse;
import com.servicedesk.entity.Role;
import com.servicedesk.entity.Status;
import com.servicedesk.entity.Ticket;
import com.servicedesk.entity.TicketComment;
import com.servicedesk.entity.User;
import com.servicedesk.exception.InvalidRequestException;
import com.servicedesk.repository.TicketCommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketService ticketService;
    private final UserService userService;

    public CommentService(TicketCommentRepository commentRepository, TicketService ticketService, UserService userService) {
        this.commentRepository = commentRepository;
        this.ticketService = ticketService;
        this.userService = userService;
    }

    public CommentResponse addComment(Long ticketId, CommentCreateRequest request) {
        Ticket ticket = ticketService.findTicketEntityById(ticketId);
        User author = userService.findUserEntityById(request.getAuthorId());

        if (author.getRole() != Role.SUPPORT_AGENT) {
            throw new InvalidRequestException("Only a SUPPORT_AGENT can add comments or resolution notes");
        }

        if (ticket.getStatus() == Status.CLOSED) {
            throw new InvalidRequestException("Cannot add comments to a CLOSED ticket");
        }

        TicketComment comment = new TicketComment();
        comment.setMessage(request.getMessage());
        comment.setTicket(ticket);
        comment.setAuthor(author);

        TicketComment saved = commentRepository.save(comment);
        return new CommentResponse(saved);
    }

    public List<CommentResponse> getCommentsForTicket(Long ticketId) {
        // Ensures the ticket exists before listing its comments.
        ticketService.findTicketEntityById(ticketId);

        return commentRepository.findByTicketIdOrderByCreatedDateAsc(ticketId).stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }
}
