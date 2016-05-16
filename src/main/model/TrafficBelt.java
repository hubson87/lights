package main.model;

import main.model.enums.DirectionEnum;
import java.util.ArrayList;
import java.util.List;

public class TrafficBelt {
    private int carsLimit;
    private List<Car> containingCars;
    private DirectionEnum beltDirection;
    private int beltXStart, beltXEnd;
    private int beltYStart, beltYEnd;

    List<TrafficLightsAndCrossing> crossingAndLights;

    public TrafficBelt(int carsLimit) {
        this.carsLimit = carsLimit;
        this.containingCars = new ArrayList<Car>();
    }

    public boolean addCar(int maxSpeed) {
        if (containingCars.size() >= carsLimit) {
            return false;
        }
        Car car = new Car(maxSpeed, beltDirection, beltXStart, beltYStart);
        return containingCars.add(car);
    }
}
