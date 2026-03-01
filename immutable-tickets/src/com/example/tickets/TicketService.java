package com.example.tickets;

import java.util.ArrayList;
import java.util.List;

/**
 * Service layer that creates tickets.
 *
 * CURRENT STATE (BROKEN ON PURPOSE):
 * - creates partially valid objects
 * - mutates after creation (bad for auditability)
 * - validation is scattered & incomplete
 *
 * TODO (student):
 * - After introducing immutable IncidentTicket + Builder, refactor this to stop mutating.
 */
public class TicketService {

    /**
     * Create a new ticket with required fields.
     * Uses the Builder to ensure validation and immutability.
     */
    public IncidentTicket createTicket(String id, String reporterEmail, String title) {
        List<String> initialTags = new ArrayList<>();
        initialTags.add("NEW");

        return new IncidentTicket.Builder()
                .id(id)
                .reporterEmail(reporterEmail)
                .title(title)
                .priority("MEDIUM")
                .source("CLI")
                .customerVisible(false)
                .tags(initialTags)
                .build();
    }

    /**
     * Create an escalated version of an existing ticket.
     * Instead of mutating, creates a new instance via toBuilder().
     */
    public IncidentTicket escalateToCritical(IncidentTicket t) {
        List<String> escalatedTags = new ArrayList<>(t.getTags());
        escalatedTags.add("ESCALATED");

        return t.toBuilder()
                .priority("CRITICAL")
                .tags(escalatedTags)
                .build();
    }

    /**
     * Assign a ticket to an agent.
     * Instead of mutating, creates a new instance via toBuilder().
     */
    public IncidentTicket assign(IncidentTicket t, String assigneeEmail) {
        return t.toBuilder()
                .assigneeEmail(assigneeEmail)
                .build();
    }
}
