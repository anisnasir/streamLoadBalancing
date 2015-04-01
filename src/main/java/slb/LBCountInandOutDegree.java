package slb;
/*
 * class to count test the scenario for skewness at the sources as well as servers
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import com.clearspring.analytics.hash.MurmurHash;

public class LBCountInandOutDegree implements LoadBalancer {
    private final List<Server> nodes;
    private final int serversNo;
    // how many "last" time slots, excluding the current one, are used to calculate the "current load"
    private final long loadSamplingGranularity;
    private final int numSources;
    private final long source_workload[];
    private int key_count;
    HashMap<Integer,BufferedWriter> bw;
    int source_counter;
 
    
    //for each load use array to store statistics
    private long localworkload[][];
    
    //private final Map<Object, Server> currentNodes = new HashMap<Object, Server>();
            	
	public LBCountInandOutDegree (List<Server> nodes, int numSources, HashMap<Integer,BufferedWriter> writer){
		this.nodes = nodes;
		key_count=0;
		source_counter=0;
		this.numSources = numSources;
		source_workload=new long[this.numSources];
		this.serversNo = nodes.size();
		this.loadSamplingGranularity = nodes.get(0).getGranularity();
		for (int i = 1; i < this.serversNo; i++){
			assert(this.loadSamplingGranularity == nodes.get(i).getGranularity());
		}
		localworkload = new long[numSources][];
		for (int i=0;i<numSources;i++)
			localworkload[i] = new long[nodes.size()];
		this.bw=writer;
	}
	/*This method is called for each edge. Vertex1 is hashed to find the source, where edge should be routed for processing 
	 * @param vertex 
	*/
	public Server getSever(long vertex1, Object key) {
		//int source = Math.abs(String.valueOf(vertex1).hashCode()%numSources);
		int source = source_counter;
		source_counter=(source_counter+1)%numSources;
		//int source = Math.abs(MurmurHash.hash64(Constants.FIRST_SEED) % numSources);
		source_workload[source]++;
		key_count++;

		if(key_count%Constants.LOG_SOURCE_SKEW_INTERVAL ==0)
		{
			for(int i=0;i<this.numSources;i++)
				logSourceLoadVector(i,this.bw.get(i));
			key_count = 0;
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
	public void logSourceLoadVector(int source, BufferedWriter bw)
	{
		try{
			for (int i=0;i<this.serversNo;i++)
				bw.write(localworkload[source][i]+" ");
			bw.write("\n");
		}catch(Exception ex)
		{
			
		}
		
	}
}
