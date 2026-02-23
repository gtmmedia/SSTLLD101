public class EventLeadTool implements ClubAdminTools {
    private final EventPlanner planner;
    public EventLeadTool(EventPlanner planner) { this.planner = planner; }

    @Override public void createEvent(String name, double budget) { planner.create(name, budget); }
    @Override public int getEventsCount() { return planner.count(); }

    @Override public void addIncome(double amt, String note) { /* irrelevant */ }
    @Override public void addExpense(double amt, String note) { /* irrelevant */ }
    @Override public void addMinutes(String text) { /* irrelevant */ }
}
