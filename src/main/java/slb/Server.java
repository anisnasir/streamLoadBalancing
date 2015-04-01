package slb;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Writer;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a single server
 */
public class Server {
    // private long startingTS;
    // private long timeSlot;
    private long currentTimestamp;
    private final long granularity;
    private final int minSize;
    private final Deque<ServerStats> timeSerie = new LinkedList<ServerStats>();

    public Server(long initialTimestamp, TimeGranularity granularity, int minSize) {
        // this.timeSlot = 0;
        this.currentTimestamp = initialTimestamp;
        this.timeSerie.add(new ServerStats());
        this.granularity = granularity.getNumberOfSeconds();
        this.minSize = (minSize > 0) ? minSize : 1; // minimum size = 1
    }

    /**
     * Updates the stats of this server with the latest timestamp and the word.
     * 
     * @param timestamp
     *            The latest timestamp
     * @param word
     *            The word
     * @return true if there are stats already completed that can be reomved
     */
    public boolean updateStats(long timestamp, String word) {
        // CONTRACT: updates have monotonically increasing timestamps
    	try{
	        checkArgument(timestamp >= currentTimestamp);
	        synch(timestamp);
	        this.timeSerie.peekLast().update(timestamp, word);
	        return timeSerie.size() > minSize; // 2 or more
    	}catch(Exception ex)
    	{
    		return false;
    	}
    }

    /**
     * Registers transition for the latest timestamp.
     * 
     * @param timestamp
     *            The latest timestamp
     * @return true if there are stats already completed that can be reomved
     */
    public boolean addTransition(long timestamp) {
        // CONTRACT: updates have monotonically increasing timestamps
    	try{
			    checkArgument(timestamp >= currentTimestamp);
			    synch(timestamp);
			    this.timeSerie.peekLast().addTransition();
			    return timeSerie.size() > minSize; // 2 or more
    	}catch(Exception ex)
    	{
    		return false;
    	}
			   
    }

    /**
     * Synchronizes stats up to the timestamp. New empty ServerStats are added to fill the gap between the last seen timestamp and the new timestamp.
     * 
     * @param newTimestamp
     *            Timestamp up to which synch stats to.
     */
    public void synch(long newTimestamp) {
        // add new elements to the time serie until we get one for the right windows
        while (this.currentTimestamp + this.granularity - 1 < newTimestamp) {
            ServerStats newStats = new ServerStats();
            this.timeSerie.addLast(newStats);
            this.currentTimestamp += this.granularity;
            // this.timeSlot++; // see if the timeslots coincide among timeseries
        }
    }

    /**
     * Prints the next time serie to the writer.
     * 
     * @param out
     *            Writer to write to.
     * @return True if printed some time series, false no time serie printed.
     */
    public boolean printNextUnused(Writer out) {
        if (timeSerie.size() <= minSize)
            return false;
        try {
            // System.out.println("Printing time serie " + this.getClass().getName() + " with position " + this.printPos + " and size " +
            // this.timeSerie.size());
            checkArgument(!timeSerie.isEmpty());
            out.write(timeSerie.pollFirst().toString());
            return true;
        } catch (IOException e) {
            System.err.println("Problem writing time serie to output file");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Prints the next time serie to the writer.
     * 
     * @param out
     *            Writer to write to.
     * @return True if printed some time series, false no time serie printed.
     */
    public boolean flushNext(Writer out) {
        checkArgument(!timeSerie.isEmpty());
        if (timeSerie.isEmpty())
            return false;
        try {
            // System.out.println("Printing time serie " + this.getClass().getName() + " with position " + this.printPos + " and size " +
            // this.timeSerie.size());
            out.write(timeSerie.pollFirst().toString());
            return true;
        } catch (IOException e) {
            System.err.println("Problem writing time serie to output file");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get "latest" stats.
     * 
     * @param timestamp
     *            Current timestamp; "latest" means "up to timestamp".
     * @param numTimeSlots
     *            Number of time slots (preceding the one of the timestamp) for stats accumulation
     * @return Stats accumulated in the previous m minutes.
     */
    public ServerStats getStats(long timestamp, int numTimeSlots) {
        // CONTRACT: gets have monotonically increasing timestamps
    	
		checkArgument(timestamp >= currentTimestamp);
		checkArgument(numTimeSlots > 0);
		synch(timestamp);
  
        Iterator<ServerStats> iter = timeSerie.descendingIterator();
        ServerStats accStats = new ServerStats();
        // we assume that we have real time load information "for free", i.e., beyond the numTimeSlots slots
        for (int i = 0; i <= numTimeSlots; i++) {
            if (!iter.hasNext())
                break;
            accStats.accumulate(iter.next());
        }
        return accStats;
    }

    public long getGranularity() {
        return granularity;
    }
}
