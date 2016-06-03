package main.model.enums;

/**
 * Created by Krzysztof Baran
 * Enumerator dla algorytmów zmiany świateł.
 */
public enum  AlgorithmType {
    /**
     * Stały czas zmiany świateł co intervał
     */
    FIXED_TIME,
    /**
     * Zmiana świateł w zależności od tego ile samochodów i jak długo czeka na pasie oraz tego jak dużo z nich nie może się na nim już zmieścić
     */
    CARS_COUNT;
}
