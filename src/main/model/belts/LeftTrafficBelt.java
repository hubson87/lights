package main.model.belts;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.image.ImageView;
import main.model.Car;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.DirectionEnum;
import main.model.results.SpeedResult;

public class LeftTrafficBelt extends TrafficBelt {
    public LeftTrafficBelt(int beltNumber, int carsLimit, int xPos, int yPos, int width, int height, Integer speedControlXStart,
                           Integer speedControlXEnd) {
        super(beltNumber, carsLimit, xPos, yPos, width, height, DirectionEnum.LEFT, speedControlXStart, speedControlXEnd);
        beltXStart = xPos + width;
        beltXEnd = xPos;
        beltYStart = beltYEnd = yPos + 3;
    }

    @Override
    protected Car findFirstCarAfterTheCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        Car firstCar = null;
        for (Car c : containingCars) {
            if (c == car) {
                continue;
            }
            if (nextCrossing.getX1() > c.getPosition().x && (firstCar == null || c.getPosition().x > firstCar.getPosition().x)) {
                firstCar = c;
            }
        }
        Car foundCloneFixed = firstCar != null ? new Car(firstCar.getX() - BELT_HEIGHT, firstCar.getY()) : null;
        return foundCloneFixed;
    }

    @Override
    protected boolean collisionBetweenTwoCars(Car car, Car c) {
        return car.getPosition().x - BELT_HEIGHT <= c.getPosition().x + BELT_HEIGHT * getStoppingDistFact() &&
            car.getPosition().x > c.getPosition().x;
    }

    @Override
    protected boolean hasDistanceToCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        return car.getPosition().x > nextCrossing.getX2() + Math.abs(car.getMaxSpeed()) * 3;
    }

    @Override
    protected synchronized boolean hasGreenLight(TrafficLightsAndCrossing nextCrossing) {
        if (nextCrossing == null) {
            return true;
        }
        return nextCrossing.isHorizontalGreen();
    }

    @Override
    protected TrafficLightsAndCrossing getNextCrossingAndLights(Point carPos) {
        TrafficLightsAndCrossing res = null;
        for (TrafficLightsAndCrossing crossing : crossingAndLights) {
            if (crossing.getX2() < carPos.x - BELT_HEIGHT && (res == null || res.getX2() < crossing.getX2())) {
                res = crossing;
            }
        }
        return res;
    }

    @Override
    protected synchronized List<ImageView> cleanup() {
        List<ImageView> carsViewsToRemove = new ArrayList<>();
        List<Car> carsToRemove = new ArrayList<>();
        containingCars.stream().filter(car -> car.getX() + car.getFitWidth() <= 0).forEach(car -> {
            carsViewsToRemove.add(car);
            carsToRemove.add(car);
            car.carRemoveLogic();
        });
        clear(carsToRemove);
        return carsViewsToRemove;
    }
}
