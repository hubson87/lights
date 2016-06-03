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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

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
    protected Map<WeatherEnum, Long> carsThatLeftTheStageWithWeather;
    protected long carsThatLeftTheStage;
    private final Integer speedControlXStart, speedControlXEnd;
    private Integer addCarTriesFailure;

    public TrafficBelt(int beltNumber, int carsLimit, int xPos, int yPos, int width, int height, DirectionEnum beltDirection,
                       Integer speedControlXStart, Integer speedControlXEnd) {
        carsThatLeftTheStageWithWeather = new HashMap<>();
        carsThatLeftTheStage = 0L;
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

    public synchronized void changeCarsSpeed(WeatherEnum weatherConditions) {
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
            for (TrafficLightsAndCrossing crossing : crossingAndLights) {
                if (crossing.isCarOnTheCrossing(car.getPosition(), BELT_HEIGHT)) {
                    crossing.addCarIfNotExists(car);
                } else if (!crossing.isCarOnTheCrossing(car.getPosition(), BELT_HEIGHT)) {
                    crossing.removeCar(car);
                }
            }
        }
        return cleanup();
    }

    private boolean canCarGo(Car car) {
        Point carPos = car.getPosition();
        TrafficLightsAndCrossing nextCrossing = getNextCrossingAndLights(carPos);
        TrafficLightsAndCrossing currentCrossing = getCurrentCrossingIfCarOnCrossing(carPos);
        if (hasAnyPossibleCollision(car)) {
            return false;
        }
        if (hasGreenLight(nextCrossing) && !hasCollisionOnTheCurrentCrossing(nextCrossing, car)) {
            return true;
        }
        if (!hasGreenLight(nextCrossing)) {
            addCarTriesFailure++;
        }
        //has red light but still has some distance to the traffic lights
        if (nextCrossing == null || hasDistanceToCrossing(car, nextCrossing)) {
            return true;
        }
        return false;
    }

    private TrafficLightsAndCrossing getCurrentCrossingIfCarOnCrossing(Point carPos) {
        for (TrafficLightsAndCrossing crossing : crossingAndLights) {
            if (crossing.isCarOnTheCrossing(carPos, BELT_HEIGHT)) {
                return crossing;
            }
        }
        return null;
    }

    private boolean hasCollisionOnTheCurrentCrossing(TrafficLightsAndCrossing crossing, Car car) {
        if (crossing == null) {
            return false;
        }
        if (hasDistanceToCrossing(car, crossing)) {
            return false;
        }
        Car tempCar = new Car(car);
        for (int i = 0; i <= crossing.getVBeltsCount() + 1; i++) {
            tempCar.go();
            for (Car c : crossing.getContainingCars()) {
                if (c == car) {
                    continue;
                }
                if (checkFullCollisionBetweenTwoCars(c, tempCar)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean checkFullCollisionBetweenTwoCars(Car c, Car tempCar) {
        boolean yCollision = ((c.getY() >= tempCar.getY() && c.getY() <= tempCar.getY() + BELT_HEIGHT) ||
                (tempCar.getY() >= c.getY() && tempCar.getY() <= c.getY() + BELT_HEIGHT));
        boolean xCollision = ((c.getX() >= tempCar.getX() && c.getX() <= tempCar.getX() + BELT_HEIGHT) ||
                (tempCar.getX() >= c.getX() && tempCar.getX() <= c.getX() + BELT_HEIGHT));
        return xCollision && yCollision;
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

    protected abstract boolean hasDistanceToCrossing(Car car, TrafficLightsAndCrossing nextCrossing) ;

    protected abstract boolean hasGreenLight(TrafficLightsAndCrossing nextCrossing) ;

    protected abstract TrafficLightsAndCrossing getNextCrossingAndLights(Point carPos);

    protected abstract List<ImageView> cleanup();

    protected void clear(List<Car> carsToRemove) {
        synchronized (containingCars) {
            containingCars.removeAll(carsToRemove);
            speedResults.addAll(carsToRemove.stream().map(car -> new SpeedResult(car.getAverageSpeed(), car.getRadarMeasuredSpeed(), car.getSpeedsForWeather()))
                .collect(Collectors.toList()));
            countWeatherCars(carsToRemove);
            carsThatLeftTheStage += carsToRemove.size();
        }
    }

    private synchronized void countWeatherCars(List<Car> carsToRemove) {
        for (Car car : carsToRemove) {
            if (!carsThatLeftTheStageWithWeather.containsKey(car.getCurrentWeather())) {
                carsThatLeftTheStageWithWeather.put(car.getCurrentWeather(), 0L);
            }
            carsThatLeftTheStageWithWeather.put(car.getCurrentWeather(), carsThatLeftTheStageWithWeather.get(car.getCurrentWeather()) + 1);
        }
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

    public Map<WeatherEnum, Long> getCarsThatLeftTheStageWithWeather() {
        return carsThatLeftTheStageWithWeather;
    }

    public long getCarsThatLeftTheStage() {
        return carsThatLeftTheStage;
    }
}
