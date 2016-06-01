package main.model.enums;

import java.util.Random;

public enum  WeatherEnum {
    SUNNY (20, 30, 40, 60, 3.0),
    RAINY (10, 20, 40, 40, 1.5),
    SNOWY (10, 20, 20, 30, 1.2),
    GLAZE (10, 10, 20, 20, 1.0),
    FOGGY (10, 20, 30, 30, 1.3);

    final int slowerMinFactor, slowerRandomFactor;
    final int fasterMinFactor, fasterRandomFactor;
    final double stoppingDistanceFactor;

    WeatherEnum(int slowerMinFactor, int slowerRandomFactor, int fasterMinFactor, int fasterRandomFactor, double stoppingDistanceFactor) {
        this.slowerMinFactor = slowerMinFactor;
        this.slowerRandomFactor = slowerRandomFactor;
        this.fasterMinFactor = fasterMinFactor;
        this.fasterRandomFactor = fasterRandomFactor;
        this.stoppingDistanceFactor = stoppingDistanceFactor;
    }

    public static WeatherEnum getRandom() {
        Random random = new Random();
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

    public int getSlowerMinFactor() {
        return slowerMinFactor;
    }

    public int getSlowerRandomFactor() {
        return slowerRandomFactor;
    }

    public int getFasterMinFactor() {
        return fasterMinFactor;
    }

    public int getFasterRandomFactor() {
        return fasterRandomFactor;
    }

    public double getStoppingDistanceFactor() {
        return stoppingDistanceFactor;
    }
}
