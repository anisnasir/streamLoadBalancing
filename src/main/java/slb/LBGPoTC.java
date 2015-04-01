package slb;

import java.util.List;
import com.clearspring.analytics.hash.MurmurHash;

public class LBGPoTC implements LoadBalancer {
    private final List<Server> nodes;
    private final int serversNo;
    // how many "last" time slots, excluding the current one, are used to calculate the "current load"
    private  long workload[];
    //private final Map<Object, Server> currentNodes = new HashMap<Object, Server>();
            	
	public LBGPoTC (List<Server> nodes){
		this.nodes = nodes;
		this.serversNo = nodes.size();
		workload = new long[nodes.size()];
	}

	/*
	 * (non-Javadoc)
	 * @see slb.LoadBalancer#getSever(long, java.lang.Object)
	 * @param timestamp timestamp of the tweets
	 * @param key data
	 */
	public Server getSever(long timestamp, Object key) {
		
		byte b[] = key.toString().getBytes();
		long nextSeed =  MurmurHash.hash64(b,b.length,Constants.FIRST_SEED);
		int firstChoice = (int)Math.abs(nextSeed % serversNo);
		
		int secondChoice;
		nextSeed = MurmurHash.hash64(b,b.length,Constants.SECOND_SEED);
		secondChoice = (int)Math.abs(nextSeed % serversNo);
		
		int selected = workload[firstChoice]>workload[secondChoice]?secondChoice:firstChoice;
		workload[selected]++;
		
		Server selectedNode = nodes.get(selected);

		return selectedNode;
	}
}
