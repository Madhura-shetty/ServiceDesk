package com.servicedesk.dto;

import com.servicedesk.entity.TicketComment;
import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private String message;
    private LocalDateTime createdDate;
    private Long ticketId;
    private UserResponse author;

    public CommentResponse() {
    }

    public CommentResponse(TicketComment comment) {
        this.id = comment.getId();
        this.message = comment.getMessage();
        this.createdDate = comment.getCreatedDate();
        this.ticketId = comment.getTicket().getId();
        this.author = new UserResponse(comment.getAuthor());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public UserResponse getAuthor() {
        return author;
    }

    public void setAuthor(UserResponse author) {
        this.author = author;
    }
}
