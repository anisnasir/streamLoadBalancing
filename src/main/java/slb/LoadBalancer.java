package slb;

public interface LoadBalancer {
    /**
     * Returns the server to which to route the key.
     * 
     * @param timestamp
     *            The current timestamp
     * @param key
     *            The key to route
     * @return The Server to route the key to
     */
    public Server getSever(long timestamp, Object key);
}
