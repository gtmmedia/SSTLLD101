import java.util.*;

public class MinutesBook {
    private final List<String> items = new ArrayList<>();
    public void add(String text) {
        items.add(text);
        System.out.println("Minutes added: \"" + text + "\"");
    }
    public int count() { return items.size(); }
}
