package main.model;

import javafx.scene.image.ImageView;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.model.enums.DirectionEnum;
import main.model.results.SpeedResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrafficBelt {
    public static final int BELT_HEIGHT = 20;
    private static final double STOPPING_DIST_FACT = 1.5;
    private int carsLimit;
    private Rectangle beltRect;
    private List<Car> containingCars;
    private DirectionEnum beltDirection;
    private int beltXStart, beltYStart, beltXEnd, beltYEnd;
    private List<TrafficLightsAndCrossing> crossingAndLights;
    private List<SpeedResult> speedResults;

    public TrafficBelt(int carsLimit, int xPos, int yPos, int width, int height, DirectionEnum beltDirection) {
        this.carsLimit = carsLimit;
        speedResults = new ArrayList<>();
        this.containingCars = new ArrayList<>();
        beltRect = new Rectangle(xPos, yPos, width, height);
        beltRect.setFill(Color.web("0x34495E"));
        beltRect.setStroke(Color.web("0xC9CED4"));
        this.beltDirection = beltDirection;
        //belt is always given in parameters as x,y from left upper corner
        beltXStart = !beltDirection.isHorizontal() ? xPos + 3 :
                (beltDirection == DirectionEnum.RIGHT ? xPos : xPos + width);
        beltYStart = beltDirection.isHorizontal() ? yPos + 3 :
                (beltDirection == DirectionEnum.DOWN ? yPos : yPos + height);
        beltXEnd = !beltDirection.isHorizontal() ? xPos + 3 :
            (beltDirection == DirectionEnum.RIGHT ? xPos + width : xPos);
        beltYEnd = beltDirection.isHorizontal() ? yPos + 3 :
            (beltDirection == DirectionEnum.DOWN ? yPos + height : yPos);
    }

    public List<TrafficLightsAndCrossing> getCrossingAndLights() {
        if (crossingAndLights == null) {
            crossingAndLights = new ArrayList<>();
        }
        return crossingAndLights;
    }

    public ImageView addCar(int maxSpeed) {
        if (containingCars.size() >= carsLimit) {
            return null;
        }
        Car car = new Car(maxSpeed, beltDirection, beltXStart, beltYStart);
        if (hasAnyPossibleCollision(car)) {
            return null;
        }
        containingCars.add(car);
        return car.getImageView();
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
        //has red light but still has some distance to the traffic lights
        if (hasDistanceToCrossing(car, nextCrossing)) {
            return true;
        }
        return false;
    }

    private boolean canGoThroughTheCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        if (nextCrossing == null) {
            return true;
        }
        return !hasDistanceToCrossing(car, nextCrossing) && hasPlaceAfterTheCrossing(car, nextCrossing);
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

    private Car findFirstCarAfterTheCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        Car firstCar = null;
        for (Car c : containingCars) {
            if (c == car) {
                continue;
            }
            if ((beltDirection == DirectionEnum.RIGHT && nextCrossing.getX2() < c.getPosition().x && (firstCar == null || c.getPosition().x < firstCar.getPosition().x))
            || (beltDirection == DirectionEnum.LEFT && nextCrossing.getX1() > c.getPosition().x && (firstCar == null || c.getPosition().x > firstCar.getPosition().x))
            || (beltDirection == DirectionEnum.DOWN && nextCrossing.getY2() < c.getPosition().y && (firstCar == null || c.getPosition().y < firstCar.getPosition().y))
            || (beltDirection == DirectionEnum.UP && nextCrossing.getY1() > c.getPosition().y && (firstCar == null || c.getPosition().y > firstCar.getPosition().y))){
                firstCar = c;
            }
        }
        return firstCar;
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

    private boolean collisionBetweenTwoCars(Car car, Car c) {
        if (beltDirection == DirectionEnum.RIGHT && car.getPosition().x + BELT_HEIGHT >= c.getPosition().x - BELT_HEIGHT * STOPPING_DIST_FACT
            && car.getPosition().x < c.getPosition().x) {
            return true;
        }
        if (beltDirection == DirectionEnum.LEFT && car.getPosition().x - BELT_HEIGHT <= c.getPosition().x + BELT_HEIGHT * STOPPING_DIST_FACT
            && car.getPosition().x > c.getPosition().x) {
            return true;
        }
        if (beltDirection == DirectionEnum.DOWN && car.getPosition().y + BELT_HEIGHT >= c.getPosition().y - BELT_HEIGHT * STOPPING_DIST_FACT
            && car.getPosition().y < c.getPosition().y) {
            return true;
        }
        if (beltDirection == DirectionEnum.UP && car.getPosition().y - BELT_HEIGHT <= c.getPosition().y + BELT_HEIGHT * STOPPING_DIST_FACT
            && car.getPosition().y > c.getPosition().y) {
            return true;
        }
        return false;
    }

    private boolean hasDistanceToCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        switch (beltDirection) {
            case RIGHT:
                return car.getPosition().x < nextCrossing.getX1() - Math.abs(car.getMaxSpeed()) * 3;
            case LEFT:
                return car.getPosition().x > nextCrossing.getX2() + Math.abs(car.getMaxSpeed())  * 3;
            case UP:
                return car.getPosition().y > nextCrossing.getY2() + Math.abs(car.getMaxSpeed())  * 3;
            case DOWN:
                return car.getPosition().y < nextCrossing.getY1() - Math.abs(car.getMaxSpeed())  * 3;
        }
        return false;
    }

    private synchronized boolean hasGreenLight(TrafficLightsAndCrossing nextCrossing) {
        //no more crossings to pass
        if (nextCrossing == null) {
            return true;
        }
        if (beltDirection == DirectionEnum.UP || beltDirection == DirectionEnum.DOWN) {
          return nextCrossing.isVerticalGreen();
        }
        return nextCrossing.isHorizontalGreen();
    }

    private TrafficLightsAndCrossing getNextCrossingAndLights(Point carPos) {
        TrafficLightsAndCrossing res = null;
        switch (beltDirection) {
            case RIGHT:
                for (TrafficLightsAndCrossing crossing : crossingAndLights) {
                    if (crossing.getX1() > carPos.x + BELT_HEIGHT && (res == null || res.getX1() > crossing.getX1())) {
                        res = crossing;
                    }
                }
                return res;
            case LEFT:
                for (TrafficLightsAndCrossing crossing : crossingAndLights) {
                    if (crossing.getX2() < carPos.x - BELT_HEIGHT && (res == null || res.getX2() < crossing.getX2())) {
                        res = crossing;
                    }
                }
                return res;
            case UP:
                for (TrafficLightsAndCrossing crossing : crossingAndLights) {
                    if (crossing.getY2() < carPos.y - BELT_HEIGHT && (res == null || res.getY2() < crossing.getY2())) {
                        res = crossing;
                    }
                }
                return res;
            case DOWN:
                for (TrafficLightsAndCrossing crossing : crossingAndLights) {
                    if (crossing.getY1() > carPos.y + BELT_HEIGHT && (res == null || res.getY1() > crossing.getY1())) {
                        res = crossing;
                    }
                }
                return res;
        }
        return null;
    }

    private synchronized List<ImageView> cleanup() {
        List<ImageView> carsViewsToRemove = new ArrayList<>();
        List<Car> carsToRemove = new ArrayList<>();
        if (beltDirection == DirectionEnum.DOWN) {
            for (Car car : containingCars) {
                if (car.getImageView().getY() >= beltYEnd) {
                    carsViewsToRemove.add(car.getImageView());
                    carsToRemove.add(car);
                }
            }
        } else if (beltDirection == DirectionEnum.UP) {
            for (Car car : containingCars) {
                if (car.getImageView().getY() + car.getImageView().getFitHeight() <= 0) {
                    carsViewsToRemove.add(car.getImageView());
                    carsToRemove.add(car);
                }
            }
        } else if (beltDirection == DirectionEnum.RIGHT) {
            for (Car car : containingCars) {
                if (car.getImageView().getX() >= beltXEnd) {
                    carsViewsToRemove.add(car.getImageView());
                    carsToRemove.add(car);
                }
            }
        } else if (beltDirection == DirectionEnum.LEFT) {
            for (Car car : containingCars) {
                if (car.getImageView().getX() + car.getImageView().getFitWidth() <= 0) {
                    carsViewsToRemove.add(car.getImageView());
                    carsToRemove.add(car);
                }
            }
        }
        synchronized (containingCars){
            containingCars.removeAll(carsToRemove);
            speedResults.addAll(carsToRemove.stream().map(car -> new SpeedResult(car.getMaxSpeedReached(), car.getAverageSpeed()))
                .collect(Collectors.toList()));
        }
        return carsViewsToRemove;
    }

    public DirectionEnum getBeltDirection() {
        return beltDirection;
    }

    public List<SpeedResult> getSpeedResults() {
        return speedResults;
    }
}
