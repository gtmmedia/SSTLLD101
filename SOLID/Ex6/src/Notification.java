public class Notification {
    public final String subject;
    public final String body;
    public final String email;
    public final String phone;

    public Notification(String subject, String body, String email, String phone) {
        this.subject = subject; this.body = body; this.email = email; this.phone = phone;
    }
}
