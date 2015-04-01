package slb;


import java.util.List;
import java.util.Random;

import com.clearspring.analytics.hash.MurmurHash;

public class TestingLocalEstimation implements LoadBalancer {
    private final List<Server> nodes;
    private final int serversNo;
    // how many "last" time slots, excluding the current one, are used to calculate the "current load"
    private final long loadSamplingGranularity;
    private final int numSources;
    
    //for each load use array to store statistics
    private static long localworkload[][];
    
    //private final Map<Object, Server> currentNodes = new HashMap<Object, Server>();
            	
	public TestingLocalEstimation (List<Server> nodes, int numSources){
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
	}

	public Server getSever(long timestamp, Object key) {
		
		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		boolean flag = rnd.nextBoolean();
		int source;
		if(flag)
		{
			rnd.setSeed(key.hashCode());
			source = Math.abs(rnd.nextInt())%numSources;
		}
		else
		{
			rnd.setSeed(System.currentTimeMillis());
			source = Math.abs(rnd.nextInt())%numSources;
		}
		
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
