package main.model.enums;

import java.util.Random;

/**
 * Created by Krzysztof Baran
 * Enumerator warunków pogodowych, trzymający ich współczynniki
 */
public enum  WeatherEnum {
    /**
     * Współczynniki pogody słonecznej
     */
    SUNNY (20, 30, 40, 60, 3.0, 6, "sunny.png"),
    /**
     * Współczynniki pogody deszczowej
     */
    RAINY (10, 20, 40, 40, 2.0, 8, "rainy.png"),
    /**
     * Współczynniki pogody podczas opadów śniegu
     */
    SNOWY (10, 20, 20, 30, 1.4, 10, "snowy.png"),
    /**
     * Współczynniki podczas gołoledzi
     */
    GLAZE (10, 10, 20, 20, 1.3, 30, "glaze.png"),
    /**
     * Współczynniki pogody mglistej
     */
    FOGGY (10, 20, 30, 30, 1.4, 10, "foggy.png");

    /**
     * Współczynniki odpowiadające za minimalną prędkość oraz maksymalną wartość losową dodawaną do minimalnej prędkości wolnejszych
     * samochodów dla danej pogody
     */
    final int slowerMinFactor, slowerRandomFactor;
    /**
     * Współczynniki odpowiadające za minimalną prędkość oraz maksymalną wartość losową dodawaną do minimalnej prędkości szybszych
     * samochodów dla danej pogody
     */
    final int fasterMinFactor, fasterRandomFactor;
    /**
     * Współczynnik odpowiadający za to jak często pojawiać będzie się szybszy samochód -> im większy, tym rzadziej (1/współczynnik)
     */
    final int fasterCarProbabilityOneTo;
    /**
     * Współczynnik hamowania pojazdów -> mówi o odległości w jakiej powinno się rozpocząć
     */
    final double stoppingDistanceFactor;
    /**
     * Nazwa pliku zawierającego znak mówiący o obecnych warunkach pogodowych
     */
    final String resourceName;
    /**
     * Generator wartości losowych, używany do losowania dynamicznego pogody
     */
    private static final Random random = new Random();

    /**
     * Konstruktor inicjujący wszystkie parametry ernumeratora
     * @param slowerMinFactor Minimalna prędkość wolniejszych samochodów dla danej pogody
     * @param slowerRandomFactor Maksymalna wartość losowa dodawana do prędkości minimalnej wolniejszych samochodów dla danej pogody
     * @param fasterMinFactor Minimalna prędkość szybszych samochodów dla danej pogody
     * @param fasterRandomFactor Maksymalna wartość losowa dodawana do prędkości minimalnej szybszych samochodów dla danej pogody
     * @param stoppingDistanceFactor Odległość w jakiej pojazd powinien rozpocząć hamowanie
     * @param fasterCarProbabilityOneTo Odwrotnie proporcjonalne prawdopodobieństwo pojawienia się szybszego samochodu dla danej pogody
     * @param resourceFileName Nazwa obrazu wczytywanego jako tablica informatyjna o danej pogodzeie
     */
    WeatherEnum(int slowerMinFactor, int slowerRandomFactor, int fasterMinFactor, int fasterRandomFactor, double stoppingDistanceFactor,
                int fasterCarProbabilityOneTo, String resourceFileName) {
        this.slowerMinFactor = slowerMinFactor;
        this.slowerRandomFactor = slowerRandomFactor;
        this.fasterMinFactor = fasterMinFactor;
        this.fasterRandomFactor = fasterRandomFactor;
        this.stoppingDistanceFactor = stoppingDistanceFactor;
        this.resourceName = resourceFileName;
        //the bigger this factor will be, the lower probability that the faster car appears
        this.fasterCarProbabilityOneTo = fasterCarProbabilityOneTo;
    }

    /**
     * Metoda pozwalająca na wylosowanie losowych warunków atmosferycznych
     * @return
     */
    public static WeatherEnum getRandom() {
        switch (random.nextInt(5)) {
            case 0:
                return SUNNY;
            case 1:
                return RAINY;
            case 2:
                return SNOWY;
            case 3:
                return GLAZE;
            default:
                return FOGGY;
        }
    }

    /**
     * Getter dla minimalnej prędkość wolniejszych samochodów dla danej pogody
     * @return Minimalna prędkość wolniejszych samochodów dla danej pogody
     */
    public int getSlowerMinFactor() {
        return slowerMinFactor;
    }

    /**
     * Getter dla maksymalnej wartości losowej dodawanej do minimalnej prędkości wolniejszych samochodów dla danej pogody
     * @return Maksymalna wartość losowa dodawana do minimalnej prędkości wolniejszych samochodów dla danej pogody
     */
    public int getSlowerRandomFactor() {
        return slowerRandomFactor;
    }

    /**
     * Getter dla minimalnej prędkość szybszych samochodów dla danej pogody
     * @return Minimalna prędkość szybszych samochodów dla danej pogody
     */
    public int getFasterMinFactor() {
        return fasterMinFactor;
    }

    /**
     * Getter dla maksymalnej wartości losowej dodawanej do minimalnej prędkości szybszych samochodów dla danej pogody
     * @return Maksymalna wartość losowa dodawana do minimalnej prędkości szybszych samochodów dla danej pogody
     */
    public int getFasterRandomFactor() {
        return fasterRandomFactor;
    }

    /**
     * Getter dla współczynnika hamowania pojazdów -> mówi o odległości w jakiej powinno się rozpocząć
     * @return Współczynnik hamowania pojazdów -> mówi o odległości w jakiej powinno się rozpocząć
     */
    public double getStoppingDistanceFactor() {
        return stoppingDistanceFactor;
    }

    /**
     * Getter dla nazwa pliku zawierającego znak mówiący o obecnych warunkach pogodowych
     * @return Nazwa pliku zawierającego znak mówiący o obecnych warunkach pogodowych
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Getter dla współczynnika odpowiadającego za to jak często pojawiać będzie się szybszy samochód -> im większy, tym rzadziej (1/współczynnik)
     * @return Współczynnik odpowiadający za to jak często pojawiać będzie się szybszy samochód -> im większy, tym rzadziej (1/współczynnik)
     */
    public int getFasterCarProbabilityOneTo() {
        return fasterCarProbabilityOneTo;
    }
}
