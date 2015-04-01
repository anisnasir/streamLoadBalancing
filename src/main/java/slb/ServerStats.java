package slb;

import java.util.HashSet;
import java.util.Set;

/**
 * Statistics for a server.
 * 
 */
public class ServerStats implements Comparable<ServerStats> {
    public static final ServerStats EMPTY_STATS = new ServerStats();
    public static final double RELATIVE_STANDARD_DEVIATION = 0.01; // 1% of error ~700kB counter

    private int wordCount = 0;
    private int transitions = 0;
    private Set<String> dictionary = new HashSet<String>();

    public void update(long timestamp, String word) {
        wordCount++;
        dictionary.add(word);
    }

    public void addTransition() {
        transitions++;
    }

    public void accumulate(ServerStats other) {
        wordCount += other.wordCount;
        dictionary.addAll(other.dictionary);
        transitions += other.transitions;
    }

    public long dictionarySize() {
        return dictionary.size();
    }

    public int compareTo(ServerStats other) {
        return this.wordCount - other.wordCount;
    }

    @Override
    public String toString() {
        return (wordCount + " " + dictionary.size() + " " + transitions + "\t");
    }
}
