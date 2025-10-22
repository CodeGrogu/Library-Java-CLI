package com.codegrogu.library.model;

import java.time.LocalDateTime;

/**
 * Represents an audit log entry in the library system.
 * Tracks actions performed by members or librarians.
 */
public class Audit {
    private int auditId;                // Unique ID for the audit entry
    private String action;              // Description of the action (e.g., "Book Borrowed")
    private int actorId;                // ID of the member or librarian who performed the action
    private String actorType;           // "Member" or "Librarian"
    private LocalDateTime timestamp;    // When the action occurred
    private String details;             // Optional extra information

    // Constructors
    public Audit() {}

    public Audit(int auditId, String action, int actorId, String actorType, LocalDateTime timestamp, String details) {
        this.auditId = auditId;
        this.action = action;
        this.actorId = actorId;
        this.actorType = actorType;
        this.timestamp = timestamp;
        this.details = details;
    }

    // Getters and Setters
    public int getAuditId() { return auditId; }
    public void setAuditId(int auditId) { this.auditId = auditId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public int getActorId() { return actorId; }
    public void setActorId(int actorId) { this.actorId = actorId; }

    public String getActorType() { return actorType; }
    public void setActorType(String actorType) { this.actorType = actorType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    // Utility
    @Override
    public String toString() {
        return "Audit{" +
                "auditId=" + auditId +
                ", action='" + action + '\'' +
                ", actorId=" + actorId +
                ", actorType='" + actorType + '\'' +
                ", timestamp=" + timestamp +
                ", details='" + details + '\'' +
                '}';
    }
}
