public class SecretaryTool implements ClubAdminTools {
    private final MinutesBook book;
    public SecretaryTool(MinutesBook book) { this.book = book; }

    @Override public void addMinutes(String text) { book.add(text); }

    @Override public void addIncome(double amt, String note) { /* irrelevant */ }
    @Override public void addExpense(double amt, String note) { /* irrelevant */ }
    @Override public void createEvent(String name, double budget) { /* irrelevant */ }
    @Override public int getEventsCount() { return 0; }
}
