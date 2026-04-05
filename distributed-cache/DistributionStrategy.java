/**
 * DistributionStrategy determines which cache node should store a given key.
 * This interface allows for pluggable distribution strategies.
 * Future implementations can include consistent hashing, ring-based routing, etc.
 */
public interface DistributionStrategy {
    /**
     * Determine which node index should store the given key
     * @param key the key to distribute
     * @param numberOfNodes total number of nodes
     * @return the node index (0-based)
     */
    int getNodeIndex(String key, int numberOfNodes);
}
