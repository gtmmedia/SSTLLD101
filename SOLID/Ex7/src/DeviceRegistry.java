import java.util.*;

public class DeviceRegistry {
    private final List<Object> devices = new ArrayList<>();

    public void add(Object d) { devices.add(d); }

    public <T> T getFirstOfType(Class<T> type, String simpleName) {
        for (Object d : devices) {
            if (d.getClass().getSimpleName().equals(simpleName) && type.isInstance(d)) {
                return type.cast(d);
            }
        }
        throw new IllegalStateException("Missing: " + simpleName);
    }
}
