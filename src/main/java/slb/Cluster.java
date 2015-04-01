package slb;

import java.util.ArrayList;

/**
 * Unused class.
 */
public class Cluster {

    private long currentTimestamp;
    // private ArrayList<TimeSerie> daySeries;
    private ArrayList<Server> hourSeries;
    private ArrayList<Server> minuteSeries;
    // private ConsistentHash<TimeSerie> dayHash;
    private ConsistentHash<Server> hourHash;
    private ConsistentHash<Server> minuteHash;

    public Cluster(int serversNo, int replicasNo, long startTimestamp) {
        currentTimestamp = startTimestamp;

        // initialize one time serie per server
        // daySeries = new ArrayList<TimeSerie>(serversNo);
        // for (int i = 0; i < serversNo; i++)
        // daySeries.add(new TimeSerie(currentTimestamp, TimeGranularity.DAY));
        hourSeries = new ArrayList<Server>(serversNo);
        for (int i = 0; i < serversNo; i++)
            hourSeries.add(new Server(currentTimestamp, TimeGranularity.HOUR, 1));
        minuteSeries = new ArrayList<Server>(serversNo);
        for (int i = 0; i < serversNo; i++)
            minuteSeries.add(new Server(currentTimestamp, TimeGranularity.MINUTE, 1));

        // store all time series related to servers in the ring, considering replicas
        // dayHash = new ConsistentHash<TimeSerie>(replicasNo, daySeries);
        hourHash = new ConsistentHash<Server>(replicasNo, hourSeries);
        minuteHash = new ConsistentHash<Server>(replicasNo, minuteSeries);
    }

    public Server getTimeSerieByHash(Object key, TimeGranularity granularity) {
        switch (granularity) {
        // case DAY:
        // return dayHash.get(key);
        case HOUR:
            return hourHash.get(key);
        case MINUTE:
            return minuteHash.get(key);
        default:
            throw new IllegalArgumentException("Illegal granularity");
        }
    }

}
