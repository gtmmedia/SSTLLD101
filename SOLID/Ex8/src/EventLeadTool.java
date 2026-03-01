public class EventLeadTool implements EventManager {
    private final EventPlanner planner;
    public EventLeadTool(EventPlanner planner) { this.planner = planner; }

    @Override public void createEvent(String name, double budget) { planner.create(name, budget); }
    @Override public int getEventsCount() { return planner.count(); }
}
