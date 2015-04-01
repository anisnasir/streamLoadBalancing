package slb;
/*
 * class to compare local and global estimation
 * this class mantains both local estimator for each source and global estimator
 */

import java.util.List;
import java.util.Random;

import com.clearspring.analytics.hash.MurmurHash;


public class DChoices implements LoadBalancer {
   
    private final List<Server> nodes;
    private final int serversNo;
    // how many "last" time slots, excluding the current one, are used to calculate the "current load"
    private final int numSources;
    private final int num_choices;
    
    //for each load use array to store statistics
    private long localworkload[][];
    int sourceCount;
    //private static long error[];
    public int seeds[];
    Random rnd = new Random(System.currentTimeMillis());
    
    //private final Map<Object, Server> currentNodes = new HashMap<Object, Server>();
            	
	public DChoices (List<Server> nodes, int numSources, int choices){
		this.nodes = nodes;
		this.numSources = numSources;
		this.serversNo = nodes.size();
		localworkload = new long[numSources][];
		for (int i=0;i<numSources;i++)
			localworkload[i] = new long[nodes.size()];
		
		sourceCount = 0;
		//error = new long[nodes.size()];
		if(choices > this.serversNo) {
		    this.num_choices = this.serversNo;
		}else {
		    this.num_choices = choices;
		}
		seeds = new int[choices];
		for(int i=0;i<seeds.length;i++) {
		    seeds[i] = rnd.nextInt();
		}
	}

	public Server getSever(long timestamp, Object key) {
		int source = (this.sourceCount++)%this.numSources;
		this.sourceCount%=this.numSources;
		
		int []choices = new int[this.num_choices];
		byte b[] = key.toString().getBytes();
		long nextSeed;
		int count = 0;
		while(count < this.num_choices) {
		    nextSeed =  MurmurHash.hash64(b,b.length,seeds[count]);
		    choices[count] = (int)Math.abs(nextSeed % serversNo);
		    count++;
		}
		
		int selectedlocal = selectMin(source,choices);
		localworkload[source][selectedlocal]++;
		Server selectedNode = nodes.get(selectedlocal);
		return selectedNode;
	}
	
	int selectMin(int source, int []arr) {
	    int index = 0;
	    for(int i =0;i<arr.length;i++) {
		if (this.localworkload[source][arr[index]] > this.localworkload[source][arr[i]]) {
		    index = i;
		}
	    }
	    //System.out.println(arr[index]);
	    return arr[index];
	}
	
	
}
