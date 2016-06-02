package main.model.results;

import java.util.List;
import java.util.Map;

import main.model.enums.WeatherEnum;

public class SpeedResult {
    private final long averageSpeed;
    private final String radarSpeed;
    private final Map<WeatherEnum, List<Long>> weatherSpeeds;

    public SpeedResult(long averageSpeed, String radarSpeed, Map weatherSpeeds) {
        this.averageSpeed = averageSpeed;
        this.radarSpeed = radarSpeed;
        this.weatherSpeeds = weatherSpeeds;
    }

    public long getAverageSpeed() {
        return averageSpeed;
    }

    public String getRadarSpeed() {
        return radarSpeed;
    }

    public Map<WeatherEnum, List<Long>> getWeatherSpeeds() {
        return weatherSpeeds;
    }
}
