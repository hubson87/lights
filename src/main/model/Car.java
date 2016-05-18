package main.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import main.model.enums.DirectionEnum;

import java.awt.*;

public class Car {
    ImageView imageView;
    int maxSpeed;
    int speed;
    Point position;
    int xDirection, yDirection; //positive => move up or right, negative => move left or down
    double acceleration;

    public Car(int maxSpeed, DirectionEnum direction, int beltXPos, int beltYPos) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction must be defined");
        }
        imageView = loadImgAndResize();
        yDirection = direction == DirectionEnum.UP ? 1 : (direction == DirectionEnum.DOWN ? -1 : 0);
        xDirection = direction == DirectionEnum.RIGHT ? 1 : (direction == DirectionEnum.LEFT ? -1 : 0);
        this.position = new Point(beltXPos, beltYPos);
        this.speed = maxSpeed / 2;
        this.acceleration = 1.5;
    }

    private ImageView loadImgAndResize() {
        Image carImg = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/car.png"));
        imageView = new ImageView(carImg);
        int maxSize = TrafficBelt.BELT_HEIGHT - 2;
        double scale = ((double)maxSize) / (carImg.getHeight() > carImg.getWidth() ? carImg.getHeight() : carImg.getWidth());
        imageView.resize(carImg.getWidth() * scale, carImg.getHeight() * scale);
        return imageView;
    }

    public Point go(){
        return calculatePosition(acceleration);
    }

    public Point stop() {
        return calculatePosition(-acceleration * 5);
    }

    private Point calculatePosition(double accelerationValue) {
        int xSpeed = calculateSpeed(xDirection, accelerationValue);
        int ySpeed = calculateSpeed(yDirection, accelerationValue);
        position.x += xSpeed;
        position.y += ySpeed;
        speed = xSpeed != 0 ? xSpeed : ySpeed;
        return position;
    }

    private int calculateSpeed(int direction, double accelerationValue) {
        double res = speed + acceleration * accelerationValue;
        if (accelerationValue > 0 && (int)res > maxSpeed) {
            return maxSpeed;
        } else if (accelerationValue < 0 && (int)res <= 0) {
            return 0;
        }
        return (int)res;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
