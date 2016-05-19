package main.model;

import javafx.scene.image.ImageView;
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
    int beltXStart, beltYStart, beltXEnd, beltYEnd;
    List<TrafficLightsAndCrossing> crossingAndLights;

    public TrafficBelt(int carsLimit, int xPos, int yPos, int width, int height, DirectionEnum beltDirection) {
        this.carsLimit = carsLimit;
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
            crossingAndLights = new ArrayList<TrafficLightsAndCrossing>();
        }
        return crossingAndLights;
    }

    public ImageView addCar(int maxSpeed) {
        if (containingCars.size() >= carsLimit) {
            return null;
        }
        Car car = new Car(maxSpeed, beltDirection, beltXStart, beltYStart);
        containingCars.add(car);
        return car.getImageView();
    }

    public Rectangle getBeltGraphics() {
        return beltRect;
    }

    public List<ImageView> moveCars() {
        for (Car car : containingCars) {
            car.go();
        }
        return cleanup();
    }

    private List<ImageView> cleanup() {
        List<ImageView> carsToRemove = new ArrayList<>();
        if (beltDirection == DirectionEnum.DOWN) {
            for (Car car : containingCars) {
                if (car.getImageView().getY() >= beltYEnd) {
                    carsToRemove.add(car.getImageView());
                }
            }
        } else if (beltDirection == DirectionEnum.UP) {
            for (Car car : containingCars) {
                if (car.getImageView().getY() + car.getImageView().getFitHeight() <= 0) {
                    carsToRemove.add(car.getImageView());
                }
            }
        } else if (beltDirection == DirectionEnum.RIGHT) {
            for (Car car : containingCars) {
                if (car.getImageView().getX() >= beltXEnd) {
                    carsToRemove.add(car.getImageView());
                }
            }
        } else if (beltDirection == DirectionEnum.LEFT) {
            for (Car car : containingCars) {
                if (car.getImageView().getX() + car.getImageView().getFitWidth() <= 0) {
                    carsToRemove.add(car.getImageView());
                }
            }
        }
        return carsToRemove;
    }
}
