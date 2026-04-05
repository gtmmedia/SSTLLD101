/**
 * ModuloDistributionStrategy uses hash(key) % numberOfNodes for distribution.
 * This is a simple approach that distributes keys uniformly across nodes.
 */
public class ModuloDistributionStrategy implements DistributionStrategy {
    @Override
    public int getNodeIndex(String key, int numberOfNodes) {
        if (numberOfNodes <= 0) {
            throw new IllegalArgumentException("Number of nodes must be greater than 0");
        }
        return Math.abs(key.hashCode()) % numberOfNodes;
    }
}
