public class SmsSender extends NotificationSender {
    public SmsSender(AuditLog audit) { super(audit); }

    @Override
    protected void doSend(Notification n) {
        // Print subject for consistency
        System.out.println("SMS -> to=" + n.phone + " subject=" + n.subject + " body=" + n.body);
        audit.add("sms sent");
    }
}
