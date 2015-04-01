package slb;

import java.util.List;
import java.util.Random;

import com.clearspring.analytics.hash.MurmurHash;

public class LBRandomPoTC implements LoadBalancer {
    private final Random rnd = new Random(123456789);
    private final List<Server> nodes;
    private final int serversNo;
    // how many "last" time slots, excluding the current one, are used to calculate the "current load"
    //private final Map<Object, Server> currentNodes = new HashMap<Object, Server>();
            	
	public LBRandomPoTC (List<Server> nodes){
		this.nodes = nodes;
		this.serversNo = nodes.size();	
	}

	public Server getSever(long timestamp, Object key) {
		
		byte b[] = key.toString().getBytes();
		long nextSeed =  MurmurHash.hash64(b,b.length,Constants.FIRST_SEED);
		int firstChoice = (int)Math.abs(nextSeed % serversNo);
		
		int secondChoice;
		nextSeed = MurmurHash.hash64(b,b.length,Constants.SECOND_SEED);
		secondChoice = (int)Math.abs(nextSeed % serversNo);
		
		rnd.setSeed(System.currentTimeMillis());
		int toss = Math.abs(rnd.nextInt()%2);

		int selected = (toss == 0) ? secondChoice : firstChoice;
		Server selectedNode = nodes.get(selected);
		return selectedNode;
	}

	
}
