package main.model.results;

public class SpeedResult {
    private final int maxSpeedReached;
    private final long averageSpeed;

    public SpeedResult(int maxSpeedReached, long averageSpeed) {
        this.maxSpeedReached = maxSpeedReached;
        this.averageSpeed = averageSpeed;
    }

    public int getMaxSpeedReached() {
        return maxSpeedReached;
    }

    public long getAverageSpeed() {
        return averageSpeed;
    }
}
