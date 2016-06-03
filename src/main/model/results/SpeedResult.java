package main.model.results;

import java.util.List;
import java.util.Map;

import main.model.enums.WeatherEnum;

/**
 * Created by Krzysztof Baran
 * Klasa reprezentująca wyniki pomiarów prędkości w aplikacji
 */
public class SpeedResult {
    /**
     * Średnia prędkość pojazdu na scenie
     */
    private final long averageSpeed;
    /**
     * Prędkość pojazdu zmierzona na pomiarze odcinkowym
     */
    private final String radarSpeed;
    /**
     * Mapa zawierająca warunki pogodowe oraz średnie prędkości zadanego pojazdu w tych warunkach
     */
    private final Map<WeatherEnum, List<Long>> weatherSpeeds;

    /**
     * Konstruktor ustawiający wszystkie parametry
     * @param averageSpeed Średnia prędkość pojazdu na scenie
     * @param radarSpeed Prędkość pojazdu zmierzona na pomiarze odcinkowym
     * @param weatherSpeeds Mapa zawierająca warunki pogodowe oraz średnie prędkości zadanego pojazdu w tych warunkach
     */
    public SpeedResult(long averageSpeed, String radarSpeed, Map weatherSpeeds) {
        this.averageSpeed = averageSpeed;
        this.radarSpeed = radarSpeed;
        this.weatherSpeeds = weatherSpeeds;
    }

    /**
     * Getter średniej prędkość pojazdu na scenie
     * @return Średnia prędkość pojazdu na scenie
     */
    public long getAverageSpeed() {
        return averageSpeed;
    }

    /**
     * Getter prędkość pojazdu zmierzonej na pomiarze odcinkowym
     * @return Prędkość pojazdu zmierzona na pomiarze odcinkowym
     */
    public String getRadarSpeed() {
        return radarSpeed;
    }

    /**
     * Getter mapy zawierającej warunki pogodowe oraz średnie prędkości zadanego pojazdu w tych warunkach
     * @return Mapa zawierająca warunki pogodowe oraz średnie prędkości zadanego pojazdu w tych warunkach
     */
    public Map<WeatherEnum, List<Long>> getWeatherSpeeds() {
        return weatherSpeeds;
    }
}
