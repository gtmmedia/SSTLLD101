import com.example.tickets.IncidentTicket;
import com.example.tickets.TicketService;

/**
 * Starter demo that shows why mutability is risky.
 *
 * After refactor:
 * - direct mutation should not compile (no setters)
 * - external modifications to tags should not affect the ticket
 * - service "updates" should return a NEW ticket instance
 */
public class TryIt {

    public static void main(String[] args) {
        TicketService service = new TicketService();

        // Create an immutable ticket
        IncidentTicket t = service.createTicket("TCK-1001", "reporter@example.com", "Payment failing on checkout");
        System.out.println("Created: " + t);
        System.out.println("Instance identity: " + System.identityHashCode(t));

        // "Update" operations return NEW instances
        IncidentTicket assigned = service.assign(t, "agent@example.com");
        System.out.println("\nAfter assign (new instance): " + assigned);
        System.out.println("Instance identity: " + System.identityHashCode(assigned));
        System.out.println("Original unchanged: " + t.getAssigneeEmail());

        IncidentTicket escalated = service.escalateToCritical(assigned);
        System.out.println("\nAfter escalate (new instance): " + escalated);
        System.out.println("Instance identity: " + System.identityHashCode(escalated));
        System.out.println("Original priority unchanged: " + t.getPriority());

        // Demonstrate that tags list is immutable
        System.out.println("\nTags from original: " + t.getTags());
        try {
            t.getTags().add("HACKED_FROM_OUTSIDE");
            System.out.println("ERROR: Tags list should be immutable!");
        } catch (UnsupportedOperationException e) {
            System.out.println("Good: External modification attempt blocked (UnsupportedOperationException)");
        }

        // Get tags from escalated ticket to verify they're separate
        System.out.println("Tags from escalated: " + escalated.getTags());
    }
}
