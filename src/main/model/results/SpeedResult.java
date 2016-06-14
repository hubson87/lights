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
     * Flaga mówiąca o tym, czy samochód miał wypadek
     */
    private final boolean hadCollision;
    /**
     * Pogoda w trakcie trwania której miał miejsce wypadek
     */
    private final WeatherEnum collisionWeather;

    /**
     * Konstruktor ustawiający wszystkie parametry
     * @param averageSpeed Średnia prędkość pojazdu na scenie
     * @param radarSpeed Prędkość pojazdu zmierzona na pomiarze odcinkowym
     * @param weatherSpeeds Mapa zawierająca warunki pogodowe oraz średnie prędkości zadanego pojazdu w tych warunkach
     * @param hadCollision Flaga mówiąca o tym, czy samochód miał wypadek
     * @param collisionWeather Pogoda w trakcie trwania której miał miejsce wypadek
     */
    public SpeedResult(long averageSpeed, String radarSpeed, Map weatherSpeeds, boolean hadCollision, WeatherEnum collisionWeather) {
        this.averageSpeed = averageSpeed;
        this.radarSpeed = radarSpeed;
        this.weatherSpeeds = weatherSpeeds;
        this.hadCollision = hadCollision;
        this.collisionWeather = collisionWeather;
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

    /**
     * Getter dla flagi mówiącej o tym, czy samochód miał wypadek
     * @return Flaga mówiąca o tym, czy samochód miał wypadek
     */
    public boolean isHadCollision() {
        return hadCollision;
    }

    /**
     * Getter dla pogody w trakcie trwania której miał miejsce wypadek
     * @return Pogoda w trakcie trwania której miał miejsce wypadek
     */
    public WeatherEnum getCollisionWeather() {
        return collisionWeather;
    }
}
