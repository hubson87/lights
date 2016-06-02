package main.model.results;

public class SpeedResult {
    private final long averageSpeed;
    private final String radarSpeed;

    public SpeedResult(long averageSpeed, String radarSpeed) {
        this.averageSpeed = averageSpeed;
        this.radarSpeed = radarSpeed;
    }

    public long getAverageSpeed() {
        return averageSpeed;
    }

    public String getRadarSpeed() {
        return radarSpeed;
    }
}
