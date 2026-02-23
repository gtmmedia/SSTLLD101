import java.util.*;

public class EventPlanner {
    private final List<String> events = new ArrayList<>();
    public void create(String name, double budget) {
        events.add(name);
        System.out.println("Event created: " + name + " (budget=" + (int)budget + ")");
    }
    public int count() { return events.size(); }
}
