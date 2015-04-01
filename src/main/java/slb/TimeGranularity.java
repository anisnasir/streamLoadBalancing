package slb;

public enum TimeGranularity {
    MINUTE(60), // needed for low-level stats in PoTC
    // TENMIN(10*60), TWENTYMIN(20*60),
    HALFHOUR(30 * 60), HOUR(60 * 60);
    // , DAY(60*60*24), INF(Integer.MAX_VALUE);

    private int seconds;

    private TimeGranularity(int seconds) {
        this.seconds = seconds;
    }

    public int getNumberOfSeconds() {
        return seconds;
    }
}
