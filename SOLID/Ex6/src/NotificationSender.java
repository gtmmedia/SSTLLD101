/**
 * NotificationSender contract:
 * - Accepts non-null Notification with non-null body, phone, email, and subject (empty allowed).
 * - Never throws for valid notifications.
 * - Never silently truncates or ignores fields; preserves meaning.
 * - All senders must log and print consistently.
 */
public abstract class NotificationSender {
    protected final AuditLog audit;
    protected NotificationSender(AuditLog audit) { this.audit = audit; }
    public final void send(Notification n) {
        if (n == null) throw new IllegalArgumentException("Notification must not be null");
        if (n.body == null || n.phone == null || n.email == null || n.subject == null)
            throw new IllegalArgumentException("Notification fields must not be null");
        doSend(n);
    }
    protected abstract void doSend(Notification n);
}
