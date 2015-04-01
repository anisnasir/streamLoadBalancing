package slb;

import java.util.List;
import java.util.Random;

import com.clearspring.analytics.hash.MurmurHash;

public class LBPoTCLocalProbing implements LoadBalancer {
   
    private final List<Server> nodes;
    private final int serversNo;
    // how many "last" time slots, excluding the current one, are used to calculate the "current load"
    private final long loadSamplingGranularity;
    private final int numSources;
    private final int probeFrequency;
    private  long lastProbeTimestamp;
    
    //for each load use array to store statistics
    private  long localworkload[][];
    private  long workload[];
    int sourceCount;
    
    //private final Map<Object, Server> currentNodes = new HashMap<Object, Server>();
            	
	public LBPoTCLocalProbing (List<Server> nodes, int numSources, int probe){
		this.nodes = nodes;
		this.probeFrequency = probe;
		this.numSources = numSources;
		this.serversNo = nodes.size();
		this.loadSamplingGranularity = TimeGranularity.MINUTE.getNumberOfSeconds();
		localworkload = new long[numSources][];
		for (int i=0;i<numSources;i++)
			localworkload[i] = new long[nodes.size()];
		
		workload = new long[nodes.size()];
		lastProbeTimestamp =0;
		sourceCount = 0;
	}

	public Server getSever(long timestamp, Object key) {
		synch(timestamp);
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
		workload[selected]++;
		Server selectedNode = nodes.get(selected);

		
		return selectedNode;
	}
	
	private void synch(long timestamp)
	{
		if(timestamp > lastProbeTimestamp+ this.probeFrequency*this.loadSamplingGranularity)
		{
			lastProbeTimestamp = timestamp;
			for(int i=0;i<this.serversNo;i++)
			{
				for (int j=0;j<this.numSources;j++)
				{
					localworkload[j][i] = (long) Math.ceil(workload[i]/this.numSources);
				}
			}
			
		}
	}
}
