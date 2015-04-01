package slb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;


public class Main {

    private static final double PRINT_INTERVAL = 1e6;

    public static void main(String[] args) throws IOException {
	//System.out.println("I was called");
        if (args.length < 5) {
        	System.err.println("Choose the type of simulator using:");
        	System.err.println("1. Random Power of two choices: <SimulatorType inFileName outFileName serversNo initialTime>");
        	System.err.println("2. MurmurHash: <SimulatorType inFileName outFileName serversNo initialTime>");
        	System.err.println("3. Consistent Hashing: <SimulatorType inFileName outFileName serversNo initialTime replicasNo>");
        	System.err.println("4. Global power of two choice with single source: <SimulatorType inFileName outFileName serversNo initialTime>");
         	System.err.println("5. Power of two choice with single source and sliding window: <SimulatorType inFileName outFileName serversNo initialTime SlidingWindowFrequencyinMinutes>");
         	System.err.println("6. Global Power of two choice with multiple sources with local estimation from data: <SimulatorType inFileName outFileName serversNo initialTime NumSources>");
         	System.err.println("7. Global Power of two choice with multiple sources with local estimation from ServerStats: <SimulatorType inFileName outFileName serversNo initialTime NumSources ProbeTime>");            
         	System.err.println("8. Global Power of two choice with multiple sources to test local estimation: <SimulatorType inFileName outFileName serversNo initialTime NumSources>");            
         	System.err.println("9. Simple Consistent Hashing hash mode num of Servers: <SimulatorType inFileName outFileName serversNo initialTime>");            
         	System.err.println("10. Counting in degree of a skewed Graph: <SimulatorType inFileName outFileName serversNo initialTime NumberSources>");            
         	System.err.println("11. D choices multiple sources to test local estimation: <SimulatorType inFileName outFileName serversNo initialTime NumSources NumChoices>"); 
         	System.err.println("\nSlidingWindowFrequencyinMinutes = m : power of two choices with stats accumulated over m minutes");
        	System.err.println("ProbeTime = k : get the workload from each server after every k minutes");	
            System.exit(1);
        }
        final int simulatorType = Integer.parseInt(args[0]);
        final String inFileName = args[1];
        final String outFileName = args[2];
        final int numServers = Integer.parseInt(args[3]);
        final long initialTime = Long.parseLong(args[4]);
        //System.out.println(initialTime);
        int numReplicas = 0 ;
        int lb= 0;
        int numSources = 0;
        int probeFrequency = 0;
        int num_choices = 0;
        EnumMap<TimeGranularity, HashMap<Integer, BufferedWriter>> timeSeries_local_load_output = new EnumMap<TimeGranularity,HashMap<Integer,BufferedWriter>>(TimeGranularity.class);

        
        if (simulatorType == 3)
        	numReplicas = Integer.parseInt(args[5]);
        else if (simulatorType == 5)
        	lb = Integer.parseInt(args[5]);
        else if (simulatorType == 6 || simulatorType == 8 || simulatorType == 10)
        	numSources = Integer.parseInt(args[5]);
        else if (simulatorType == 7)
        {
        	numSources = Integer.parseInt(args[5]);
        	probeFrequency = Integer.parseInt(args[6]);
        }else if(simulatorType == 11) {
            	numSources = Integer.parseInt(args[5]);
    		num_choices = Integer.parseInt(args[6]);
        }
        
        
        // initialize numServers Servers per TimeGranularity
        EnumMap<TimeGranularity, List<Server>> timeSeries = new EnumMap<TimeGranularity, List<Server>>(TimeGranularity.class);
        for (TimeGranularity tg : TimeGranularity.values()) {
            List<Server> list = new ArrayList<Server>(numServers);
            for (int i = 0; i < numServers; i++)
                list.add(new Server(initialTime, tg, lb));
            timeSeries.put(tg, list);
        }

        // initialize one output file per TimeGranularity
        EnumMap<TimeGranularity, BufferedWriter> outputs = new EnumMap<TimeGranularity, BufferedWriter>(TimeGranularity.class);
        for (TimeGranularity tg : TimeGranularity.values()) {
            outputs.put(tg, new BufferedWriter(new FileWriter(outFileName + "_" + tg.toString() + ".txt")));
        }

        // initialize one LoadBalancer per TimeGranularity
        EnumMap<TimeGranularity, LoadBalancer> hashes = new EnumMap<TimeGranularity, LoadBalancer>(TimeGranularity.class);
        for (TimeGranularity tg : TimeGranularity.values()) {
        	if(simulatorType == 1){
        		hashes.put(tg, new LBRandomPoTC(timeSeries.get(tg)));
        	}else if (simulatorType == 2) {
                hashes.put(tg, new LBMurmurHash(timeSeries.get(tg)));
        	}else if (simulatorType == 3) {
                hashes.put(tg, new LBConsistentHash(numReplicas, timeSeries.get(tg)));
            }else if (simulatorType == 4){
                hashes.put(tg, new LBGPoTC(timeSeries.get(tg)));
            }else if (simulatorType == 5){
            	hashes.put(tg, new LBSWPoTC(timeSeries.get(tg),lb));
            }else if (simulatorType == 6){
            	hashes.put(tg, new LBPoTCLocalEstimation(timeSeries.get(tg),numSources));
            }else if (simulatorType == 7){
            	hashes.put(tg, new LBPoTCLocalProbing(timeSeries.get(tg),numSources,probeFrequency));
            }else if (simulatorType == 8){
            	hashes.put(tg, new CompareLocalGlobal(timeSeries.get(tg),numSources));
            }else if (simulatorType == 9){
            	hashes.put(tg, new LBSimpleConsistentHash(timeSeries.get(tg)));
            }else if (simulatorType == 10){
            	HashMap<Integer, BufferedWriter> local_load_output = new HashMap<Integer, BufferedWriter>();
                for (int i=0;i<numSources;i++) {
                	String sep = "_";
                    local_load_output.put(i, new BufferedWriter(new FileWriter("/mnt/interns/auddin/output_new_latest/load_estimator/local_load_estimator"+sep+i+sep+numServers+sep+numSources+sep+tg.toString()+".out")));
                }
            	hashes.put(tg, new LBCountInandOutDegree(timeSeries.get(tg),numSources,local_load_output));
            	timeSeries_local_load_output.put(tg, local_load_output);
            }else if (simulatorType == 11){
            	hashes.put(tg, new DChoices(timeSeries.get(tg),numSources,num_choices));
            }
        }

        // read items and route them to the correct server
        System.out.println("Starting to read the item stream");
        BufferedReader in = null;
        try {
            InputStream rawin = new FileInputStream(inFileName);
            if (inFileName.endsWith(".gz"))
                rawin = new GZIPInputStream(rawin);
            in = new BufferedReader(new InputStreamReader(rawin));
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            e.printStackTrace();
            System.exit(1);
        }

        // core loop
        long simulationStartTime = System.currentTimeMillis();
        StreamItemReader reader = new StreamItemReader(in);
        System.out.println("I will read item");
        StreamItem item = reader.nextItem();
        //System.out.println(item.getTimestamp());
        long currentTimestamp = 0;
        int itemCount = 0;
        while (item != null) {
            if (++itemCount % PRINT_INTERVAL == 0) {
                System.out.println("Read " + itemCount / 1000000 + "M tweets.\tSimulation time: " + (System.currentTimeMillis() - simulationStartTime) / 1000
                        + " seconds");
                for (BufferedWriter bw : outputs.values())
                    // flush output every PRINT_INTERVAL items
                    bw.flush();
            }

            currentTimestamp =item.getTimestamp();
            EnumSet<TimeGranularity> statsToConsume = EnumSet.noneOf(TimeGranularity.class); // empty set of time series
            
            for (int i = 0; i < item.getWordsSize(); i++) {
                String word = item.getWord(i);
                for (Entry<TimeGranularity, LoadBalancer> entry : hashes.entrySet()) {
                    LoadBalancer loadBalancer = entry.getValue();
                    Server server = loadBalancer.getSever(currentTimestamp, word);
                    //System.out.println(word+":"+server.toString());
                    boolean hasStatsReady = server.updateStats(currentTimestamp, word);
                    if (hasStatsReady)
                        statsToConsume.add(entry.getKey());
                   
                }
            }

            for (TimeGranularity key : statsToConsume) {
                printStatsToConsume(timeSeries.get(key), outputs.get(key), currentTimestamp);
            }
            
            item = reader.nextItem();
            
        }

        // print final stats
        for (TimeGranularity tg : TimeGranularity.values()) {
            flush(timeSeries.get(tg), outputs.get(tg), currentTimestamp);
        }

        // close all files
        in.close();
        for (BufferedWriter bw : outputs.values())
            bw.close();
        
        if (simulatorType == 10)
        {
        	for(TimeGranularity tg:TimeGranularity.values())
        		for(int i=0;i<numSources;i++)
        			timeSeries_local_load_output.get(tg).get(i).close();
        }
        System.out.println("Finished reading items\nTotal items: " + itemCount);
    }

    /**
     * Prints stats from time serie to out.
     * 
     * @param servers
     *            The series to print.
     * @param out
     *            The writer to print to.
     * @param timestamp
     *            Time up to which to print.
     * @throws IOException
     */
    private static void printStatsToConsume(Iterable<Server> servers, BufferedWriter out, long timestamp) throws IOException {
        for (Server sever : servers) { // sync all servers to the current timestamp
            sever.synch(timestamp);
        }
        boolean hasMore = false;
        do {
            for (Server server : servers) { // print up to the point in which all the servers have stats ready (AND barrier)
                hasMore &= server.printNextUnused(out);
            }
            out.newLine();
        } while (hasMore);
    }

    private static void flush(Iterable<Server> series, BufferedWriter out, long timestamp) throws IOException {
        for (Server serie : series) { // sync all servers to the current timestamp
            serie.synch(timestamp);
        }
        boolean hasMore = false;
        do {
            for (Server serie : series) {
                hasMore &= serie.flushNext(out);
            }
            out.newLine();
        } while (hasMore);
    }
}
