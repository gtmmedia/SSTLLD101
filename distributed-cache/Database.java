/**
 * Database interface represents an external data source.
 * This is an abstraction for fetching data on cache misses.
 */
public interface Database {
    /**
     * Fetch value from database
     * @param key the key to fetch
     * @return the value associated with the key
     */
    Object getValue(String key);
    
    /**
     * Store/update value in database
     * @param key the key to store
     * @param value the value to store
     */
    void setValue(String key, Object value);
}
