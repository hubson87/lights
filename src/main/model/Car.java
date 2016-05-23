package main.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import main.model.enums.DirectionEnum;

import java.awt.*;

public class Car {
    ImageView imageView;
    final int maxSpeed;
    int speed;
    Point position;
    int xDirection, yDirection; //positive => move up or right, negative => move left or down
    double acceleration;

    public Car(int maxSpeed, DirectionEnum direction, int beltXPos, int beltYPos) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction must be defined");
        }
        imageView = loadImgAndResize(beltXPos, beltYPos);
        xDirection = direction == DirectionEnum.RIGHT ? 1 : (direction == DirectionEnum.LEFT ? -1 : 0);
        yDirection = direction == DirectionEnum.DOWN ? 1 : (direction == DirectionEnum.UP ? -1 : 0);
        this.position = new Point(beltXPos, beltYPos);
        this.maxSpeed = maxSpeed;
        this.speed = maxSpeed / 4;
        this.acceleration = 1.5;
    }

    private ImageView loadImgAndResize(int startPosX, int startPosY) {
        Image carImg = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/car.png"));
        imageView = new ImageView(carImg);
        int maxSize = TrafficBelt.BELT_HEIGHT - 2;
        double scale = ((double)maxSize) / (carImg.getHeight() > carImg.getWidth() ? carImg.getHeight() : carImg.getWidth());
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(carImg.getWidth() * scale);
        imageView.setFitHeight(carImg.getHeight() * scale);
        imageView.setX(startPosX);
        imageView.setY(startPosY);
        return imageView;
    }

    public void go(){
        Point newPos = calculatePosition(acceleration);
        imageView.setX(newPos.getX());
        imageView.setY(newPos.getY());
    }

    public void stop() {
        Point newPos = calculatePosition(-acceleration * 5);
        imageView.setX(newPos.getX());
        imageView.setY(newPos.getY());
    }

    private Point calculatePosition(double accelerationValue) {
        int xSpeed = calculateSpeed(xDirection, accelerationValue);
        int ySpeed = calculateSpeed(yDirection, accelerationValue);
        position.x += xSpeed;
        position.y += ySpeed;
        speed = xSpeed != 0 ? xSpeed : ySpeed;
        if ((speed < 0 && (xDirection == 1 || yDirection == 1)) || (speed > 0 && (xDirection == -1 || yDirection == -1))) {
            speed = 0;
        }
        return position;
    }

    private int calculateSpeed(int direction, double accelerationValue) {
        if (direction == 0) {
            return 0;
        }
        double res = speed + direction * accelerationValue;
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

    public Point getPosition() {
        return new Point((int)imageView.getX(), (int)imageView.getY());
    }
}
