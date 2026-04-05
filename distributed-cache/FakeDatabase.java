import java.util.*;

/**
 * FakeDatabase is a mock implementation of Database for testing.
 * It simulates a database in memory.
 */
public class FakeDatabase implements Database {
    private final Map<String, Object> data;
    
    public FakeDatabase() {
        this.data = new HashMap<>();
    }
    
    @Override
    public Object getValue(String key) {
        return data.get(key);
    }
    
    @Override
    public void setValue(String key, Object value) {
        data.put(key, value);
    }
    
    /**
     * Initialize database with some test data
     */
    public void init(String key, Object value) {
        data.put(key, value);
    }
    
    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
    }
}
