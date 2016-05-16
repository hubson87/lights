package main.model;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import main.model.enums.DirectionEnum;
import java.util.ArrayList;
import java.util.List;

public class TrafficBelt {
    public static final int BELT_HEIGHT = 20;
    private int carsLimit;
    private Rectangle beltRect;
    private List<Car> containingCars;
    private DirectionEnum beltDirection;
    int beltXStart, beltYStart;
    List<TrafficLightsAndCrossing> crossingAndLights;

    public TrafficBelt(int carsLimit, int xPos, int yPos, int width, int height, DirectionEnum beltDirection) {
        this.carsLimit = carsLimit;
        this.containingCars = new ArrayList<Car>();
        beltRect = new Rectangle(xPos, yPos, width, height);
        beltRect.setFill(Color.web("0x34495E"));
        beltRect.setStroke(Color.web("0xC9CED4"));
        this.beltDirection = beltDirection;
        //belt is always given in parameters as x,y from left upper corner
        beltXStart = !beltDirection.isHorizontal() ? xPos :
                (beltDirection == DirectionEnum.RIGHT ? xPos : xPos + width);
        beltYStart = beltDirection.isHorizontal() ? yPos :
                (beltDirection == DirectionEnum.DOWN ? yPos : yPos + height);
    }

    public List<TrafficLightsAndCrossing> getCrossingAndLights() {
        if (crossingAndLights == null) {
            crossingAndLights = new ArrayList<TrafficLightsAndCrossing>();
        }
        return crossingAndLights;
    }

    public boolean addCar(int maxSpeed) {
        if (containingCars.size() >= carsLimit) {
            return false;
        }
        Car car = new Car(maxSpeed, beltDirection, beltXStart, beltYStart);
        return containingCars.add(car);
    }

    public Rectangle getBeltGraphics() {
        return beltRect;
    }
}
