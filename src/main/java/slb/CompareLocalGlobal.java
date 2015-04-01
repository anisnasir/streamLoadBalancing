package slb;
/*
 * class to compare local and global estimation
 * this class mantains both local estimator for each source and global estimator
 */

import java.util.List;

import com.clearspring.analytics.hash.MurmurHash;


public class CompareLocalGlobal implements LoadBalancer {
   
    private final List<Server> nodes;
    private final int serversNo;
    // how many "last" time slots, excluding the current one, are used to calculate the "current load"
    private final int numSources;
    
    //for each load use array to store statistics
    private long localworkload[][];
    private long workload[]; 
    int sourceCount;
    //private static long error[];
    
    //private final Map<Object, Server> currentNodes = new HashMap<Object, Server>();
            	
	public CompareLocalGlobal (List<Server> nodes, int numSources){
		this.nodes = nodes;
		this.numSources = numSources;
		this.serversNo = nodes.size();
		localworkload = new long[numSources][];
		for (int i=0;i<numSources;i++)
			localworkload[i] = new long[nodes.size()];
		
		workload = new long[nodes.size()];
		sourceCount = 0;
		//error = new long[nodes.size()];
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
		
		
		
		int selectedlocal = localworkload[source][firstChoice]>localworkload[source][secondChoice]?secondChoice:firstChoice;
		localworkload[source][selectedlocal]++;
		int selectedglobal = workload[firstChoice]>workload[secondChoice]?secondChoice:firstChoice;
		workload[selectedlocal]++;
		Server selectedNode = nodes.get(selectedlocal);
		if (selectedglobal != selectedlocal)
			{
			selectedNode.addTransition(timestamp);
			}
		//System.out.println(key.toString()+" "+selectedlocal);
		return selectedNode;
	}
	
	
}
