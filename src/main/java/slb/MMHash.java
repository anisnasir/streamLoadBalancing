package slb;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import com.clearspring.analytics.hash.MurmurHash;

// CONTRACT: keys must implement toString()
public class MMHash<T> {
    private final int numberOfServers;
    private Map<Integer,T> serverMap = new HashMap<Integer,T>(); 

    public MMHash(Collection<T> nodes) {
        this.numberOfServers = nodes.size();
        
        int serverId = 0;
        for (T node : nodes) {
        	serverMap.put(serverId++, node);
        }
    }

    public T get(Object key) {
        int hash = (Math.abs(MurmurHash.hash(key.toString().getBytes()))%numberOfServers);
        return serverMap.get(hash);
    }

}
