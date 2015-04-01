package slb;


import java.util.List;
import java.util.Random;

import com.clearspring.analytics.hash.MurmurHash;


public class LBPoTCLocalEstimation implements LoadBalancer {
    private final List<Server> nodes;
    private final int serversNo;
    // how many "last" time slots, excluding the current one, are used to calculate the "current load"
    private final long loadSamplingGranularity;
    private final int numSources;
    private Random rnd; 
    //for each load use array to store statistics
    private long localworkload[][];
    int sourceCount;
    
    //private final Map<Object, Server> currentNodes = new HashMap<Object, Server>();
            	
	public LBPoTCLocalEstimation (List<Server> nodes, int numSources){
		this.nodes = nodes;
		this.numSources = numSources;
		this.serversNo = nodes.size();
		this.loadSamplingGranularity = nodes.get(0).getGranularity();
		for (int i = 1; i < this.serversNo; i++){
			assert(this.loadSamplingGranularity == nodes.get(i).getGranularity());
		}
		localworkload = new long[numSources][];
		for (int i=0;i<numSources;i++)
			localworkload[i] = new long[nodes.size()];
		rnd = new Random();
		sourceCount = 0;
	}

	public Server getSever(long timestamp, Object key) {
		
		int source = (this.sourceCount++)%this.numSources;
		this.sourceCount%=this.numSources;
		
		byte b[] = key.toString().getBytes();
		long nextSeed =  MurmurHash.hash64(b,b.length,Constants.FIRST_SEED);
		int firstChoice = (int)Math.abs(nextSeed % serversNo);
		
		int secondChoice;
		nextSeed = MurmurHash.hash64(b,b.length,Constants.SECOND_SEED);
		secondChoice = (int)Math.abs(nextSeed % serversNo);
		
		int selected = localworkload[source][firstChoice]>localworkload[source][secondChoice]?secondChoice:firstChoice;
		localworkload[source][selected]++;
		Server selectedNode = nodes.get(selected);
	
		return selectedNode;
	}
}
