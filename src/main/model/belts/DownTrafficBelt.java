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

public class DownTrafficBelt extends TrafficBelt {
    public DownTrafficBelt(int beltNumber, int carsLimit, int xPos, int yPos, int width, int height, Integer speedControlXStart,
                           Integer speedControlXEnd) {
        super(beltNumber, carsLimit, xPos, yPos, width, height, DirectionEnum.DOWN, speedControlXStart, speedControlXEnd);
        beltXStart = beltXEnd = xPos + 3;
        beltYStart = yPos;
        beltYEnd = yPos + height;
    }

    @Override
    protected Car findFirstCarAfterTheCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        Car firstCar = null;
        for (Car c : containingCars) {
            if (c == car) {
                continue;
            }
            if (nextCrossing.getY2() < c.getPosition().y && (firstCar == null || c.getPosition().y < firstCar.getPosition().y)) {
                firstCar = c;
            }
        }
        return firstCar;
    }

    @Override
    protected boolean collisionBetweenTwoCars(Car car, Car c) {
        return car.getPosition().y + BELT_HEIGHT >= c.getPosition().y - BELT_HEIGHT * getStoppingDistFact() &&
            car.getPosition().y < c.getPosition().y;
    }

    @Override
    protected boolean hasDistanceToCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        return car.getPosition().y < nextCrossing.getY1() - Math.abs(car.getMaxSpeed()) * 3;
    }

    @Override
    protected synchronized boolean hasGreenLight(TrafficLightsAndCrossing nextCrossing) {
        //no more crossings to pass
        if (nextCrossing == null) {
            return true;
        }
        return nextCrossing.isVerticalGreen();
    }

    @Override
    protected TrafficLightsAndCrossing getNextCrossingAndLights(Point carPos) {
        TrafficLightsAndCrossing res = null;
        for (TrafficLightsAndCrossing crossing : crossingAndLights) {
            if (crossing.getY1() > carPos.y + BELT_HEIGHT && (res == null || res.getY1() > crossing.getY1())) {
                res = crossing;
            }
        }
        return res;
    }

    @Override
    protected synchronized List<ImageView> cleanup() {
        List<ImageView> carsViewsToRemove = new ArrayList<>();
        List<Car> carsToRemove = new ArrayList<>();
        containingCars.stream().filter(car -> car.getY() >= beltYEnd).forEach(car -> {
            carsViewsToRemove.add(car);
            carsToRemove.add(car);
            car.carRemoveLogic();
        });
        clear(carsToRemove);
        return carsViewsToRemove;
    }
}
