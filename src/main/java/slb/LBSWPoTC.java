package slb;

/*
 * power of two choices using Sliding Window
 */
import java.util.List;

import com.clearspring.analytics.hash.MurmurHash;

public class LBSWPoTC implements LoadBalancer {
    private final List<Server> nodes;
    private final int serversNo;
    // how many "last" time slots, excluding the current one, are used to calculate the "current load"
    private final int loadSamplingFrequency;
    private final long loadSamplingGranularity;
    private final ServerStats[] bufferStats;
    private final long [] bufferTS;
    //private final Map<Object, Server> currentNodes = new HashMap<Object, Server>();
            	
	public LBSWPoTC (List<Server> nodes, int frequency){
		this.nodes = nodes;
		this.serversNo = nodes.size();
		this.loadSamplingFrequency = frequency;
		this.loadSamplingGranularity = TimeGranularity.MINUTE.getNumberOfSeconds();
		
		bufferStats = new ServerStats[nodes.size()];
		bufferTS = new long[nodes.size()];
	}

	public Server getSever(long timestamp, Object key) {
		int nextSeed = MurmurHash.hash(key);
		int firstChoice = Math.abs(nextSeed % serversNo);
		int secondChoice;
		do{
			nextSeed = MurmurHash.hash(nextSeed);
			secondChoice = Math.abs(nextSeed % serversNo);
		} while (firstChoice == secondChoice);
		
		ServerStats firstLoad = bufferedStats(timestamp, firstChoice);
		ServerStats secondLoad = bufferedStats(timestamp, secondChoice);
		
		int selected = firstLoad.compareTo(secondLoad) > 0 ? secondChoice : firstChoice;
		Server selectedNode = nodes.get(selected);

		
		return selectedNode;
	}

	private ServerStats bufferedStats(long timestamp, int nodeID){
		// use buffered info if available
		if (bufferTS[nodeID] + loadSamplingFrequency * loadSamplingGranularity - 1 < timestamp){
			Server serie = nodes.get(nodeID);
			bufferStats[nodeID] = serie.getStats(timestamp, loadSamplingFrequency);
			bufferTS[nodeID] = timestamp;
		}
		return bufferStats[nodeID];
		// TODO (maybe) in case of gaps, update accumulated stats until last period (not until now)		
	}
}
