package main.model.belts;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.model.Car;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.DirectionEnum;
import main.model.enums.WeatherEnum;
import main.model.results.SpeedResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class TrafficBelt {
    private static final Integer MAX_TRIES_FOR_LIGHTS_SWITCH_ALGORITHM = 200;
    private final Random random = new Random();
    public static final int BELT_HEIGHT = 20;
    private static Double STOPPING_DIST_FACT = 1.5;
    private final int beltNumber;
    private int carsLimit;
    private Rectangle beltRect;
    protected List<Car> containingCars;
    private DirectionEnum beltDirection;
    protected int beltXStart, beltYStart, beltXEnd, beltYEnd;
    protected List<TrafficLightsAndCrossing> crossingAndLights;
    protected List<SpeedResult> speedResults;
    protected int carsThatLeftTheStage;
    private final Integer speedControlXStart, speedControlXEnd;
    private Integer addCarTriesFailure;

    public TrafficBelt(int beltNumber, int carsLimit, int xPos, int yPos, int width, int height, DirectionEnum beltDirection,
                       Integer speedControlXStart, Integer speedControlXEnd) {
        carsThatLeftTheStage = 0;
        addCarTriesFailure = 0;
        this.beltNumber = beltNumber;
        this.carsLimit = carsLimit;
        speedResults = new ArrayList<>();
        this.containingCars = new ArrayList<>();
        beltRect = new Rectangle(xPos, yPos, width, height);
        beltRect.setFill(Color.web("0x34495E"));
        beltRect.setStroke(Color.web("0xC9CED4"));
        this.beltDirection = beltDirection;
        this.speedControlXStart = speedControlXStart;
        this.speedControlXEnd = speedControlXEnd;
    }

    public List<TrafficLightsAndCrossing> getCrossingAndLights() {
        if (crossingAndLights == null) {
            crossingAndLights = new ArrayList<>();
        }
        return crossingAndLights;
    }

    public synchronized ImageView addCar(WeatherEnum weatherConditions) {
        if (containingCars.size() >= carsLimit) {
            addCarTriesFailure++;
            return null;
        }
        Car car = new Car(randomMaxSpeedForCar(weatherConditions), beltDirection, beltXStart, beltYStart, speedControlXStart, speedControlXEnd,
            weatherConditions);
        if (hasAnyPossibleCollision(car)) {
            return null;
        }
        containingCars.add(car);
        return car;
    }

    public void changeCarsSpeed(WeatherEnum weatherConditions) {
        for (Car car : containingCars) {
            car.changeMaxSpeed(randomMaxSpeedForCar(weatherConditions), weatherConditions);
        }
    }

    private synchronized int randomMaxSpeedForCar(WeatherEnum weatherConditions) {
        if (random.nextInt(weatherConditions.getFasterCarProbabilityOneTo()) == 1) {
            return random.nextInt(weatherConditions.getFasterRandomFactor()) + weatherConditions.getFasterMinFactor();
        }
        return random.nextInt(weatherConditions.getSlowerRandomFactor()) + weatherConditions.getSlowerMinFactor();
    }

    public Rectangle getBeltGraphics() {
        return beltRect;
    }

    public List<ImageView> moveCars() {
        for (Car car : containingCars) {
            if (canCarGo(car)) {
                car.go();
            } else {
                car.stop();
            }
        }
        return cleanup();
    }

    private boolean canCarGo(Car car) {
        Point carPos = car.getPosition();
        TrafficLightsAndCrossing nextCrossing = getNextCrossingAndLights(carPos);
        if (hasAnyPossibleCollision(car)) {
            return false;
        }
        if (hasGreenLight(nextCrossing) && canGoThroughTheCrossing(car, nextCrossing)) {
            return true;
        }
        if (!hasGreenLight(nextCrossing)) {
            addCarTriesFailure++;
        }
        //has red light but still has some distance to the traffic lights
        if (hasDistanceToCrossing(car, nextCrossing)) {
            return true;
        }
        return false;
    }

    private boolean canGoThroughTheCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        if (nextCrossing == null || hasDistanceToCrossing(car, nextCrossing)) {
            return true;
        }
        return hasPlaceAfterTheCrossing(car, nextCrossing);
    }

    private boolean hasPlaceAfterTheCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        Car afterCrossingCar = findFirstCarAfterTheCrossing(car, nextCrossing);
        if (afterCrossingCar == null) {
            return true;
        }
        if (collisionBetweenTwoCars(car, afterCrossingCar)) {
            return false;
        }
        return true;
    }

    protected Car findFirstCarAfterTheCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        return null;
    }

    private boolean hasAnyPossibleCollision(Car car) {
        for (Car c : containingCars) {
            if (c == car) {
                continue;
            }
            if (collisionBetweenTwoCars(car, c)) {
                return true;
            }
        }
        return false;
    }

    protected boolean collisionBetweenTwoCars(Car car, Car c) {
        return false;
    }

    protected boolean hasDistanceToCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        return false;
    }

    protected synchronized boolean hasGreenLight(TrafficLightsAndCrossing nextCrossing) {
        return false;
    }

    protected TrafficLightsAndCrossing getNextCrossingAndLights(Point carPos) {
        return null;
    }

    protected synchronized List<ImageView> cleanup() {
        return null;
    }

    public DirectionEnum getBeltDirection() {
        return beltDirection;
    }

    public List<SpeedResult> getSpeedResults() {
        return speedResults;
    }

    public static double getStoppingDistFact() {
        synchronized (STOPPING_DIST_FACT) {
            return STOPPING_DIST_FACT;
        }
    }

    public static void setStoppingDistFact(double stoppingDistFact) {
        synchronized (STOPPING_DIST_FACT) {
            STOPPING_DIST_FACT = stoppingDistFact;
        }
    }

    public int getBeltNumber() {
        return beltNumber;
    }

    public synchronized boolean isAboveMaxTries() {
        return this.addCarTriesFailure > MAX_TRIES_FOR_LIGHTS_SWITCH_ALGORITHM;
    }

    public synchronized void resetCarsTriesCounter() {
        this.addCarTriesFailure = 0;
    }
}
