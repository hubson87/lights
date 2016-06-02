package main.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import main.model.enums.DirectionEnum;

import java.awt.*;
import java.time.LocalDateTime;

public class Car extends ImageView {
    private Integer maxSpeed;
    private int speed;
    private Point position;
    private int xDirection, yDirection; //positive => move down or right, negative => move left or up
    private final Point beginPos;
    private Point endPos;
    private final LocalDateTime carEnterOnStageTime;
    private LocalDateTime carLeftTheStageTime;
    private double acceleration;
    private long allSpeedsMeasured = 0L;
    private int allMovesCount = 0;
    private int maxSpeedReached = 0;
    private LocalDateTime radarSpeedMeasureStarted;
    private LocalDateTime radarSpeedMeasureEnd;
    private Integer radarSpeedStartX, radarSpeedEndX;

    public Car(int maxSpeed, DirectionEnum direction, int beltXPos, int beltYPos, Integer radarSpeedStartX, Integer radarSpeedEndX) {
        super();
        if (direction == null) {
            throw new IllegalArgumentException("Direction must be defined");
        }
        loadImgAndResize(beltXPos, beltYPos);
        xDirection = direction == DirectionEnum.RIGHT ? 1 : (direction == DirectionEnum.LEFT ? -1 : 0);
        yDirection = direction == DirectionEnum.DOWN ? 1 : (direction == DirectionEnum.UP ? -1 : 0);
        this.position = new Point(beltXPos, beltYPos);
        this.beginPos = new Point(beltXPos, beltYPos);
        this.carEnterOnStageTime = LocalDateTime.now();
        this.maxSpeed = maxSpeed;
        this.speed = maxSpeed / 4;
        this.acceleration = (double)maxSpeed / 10.0;
        this.radarSpeedStartX = radarSpeedStartX;
        this.radarSpeedEndX = radarSpeedEndX;
    }

    private void loadImgAndResize(int startPosX, int startPosY) {
        Image carImg = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/car.png"));
        setImage(carImg);
        int maxSize = TrafficBelt.BELT_HEIGHT - 2;
        double scale = ((double)maxSize) / (carImg.getHeight() > carImg.getWidth() ? carImg.getHeight() : carImg.getWidth());
        setPreserveRatio(true);
        setFitWidth(carImg.getWidth() * scale);
        setFitHeight(carImg.getHeight() * scale);
        setX(startPosX);
        setY(startPosY);
    }

    public void go(){
        synchronized (maxSpeed) {
            Point newPos = calculatePosition(acceleration);
            setX(newPos.getX());
            setY(newPos.getY());
        }
    }

    public void stop() {
        synchronized (maxSpeed) {
            Point newPos = calculatePosition(-acceleration * 5);
            setX(newPos.getX());
            setY(newPos.getY());
        }
    }

    private Point calculatePosition(double accelerationValue) {
        int xSpeed = calculateSpeed(xDirection, accelerationValue, speed);
        int ySpeed = calculateSpeed(yDirection, accelerationValue, speed);
        speed = xSpeed != 0 ? xSpeed : ySpeed;
        position.x += xSpeed;
        position.y += ySpeed;
        int absSpeed = Math.abs(speed);
        if (absSpeed > maxSpeedReached) {
            maxSpeedReached = absSpeed;
        }
        allSpeedsMeasured += absSpeed;
        ++allMovesCount;
        checkAndMarkRadars(position);
        return position;
    }

    private void checkAndMarkRadars(Point position) {
        if (xDirection == 0) {
            return;
        }
        if (xDirection > 0 && radarSpeedMeasureEnd == null) {   //so the measurement is not over yet
            if (position.x > radarSpeedStartX && radarSpeedMeasureStarted == null) {
                radarSpeedMeasureStarted = LocalDateTime.now();
            }
            if (position.x > radarSpeedEndX) {
                radarSpeedMeasureEnd = LocalDateTime.now();
            }
        }

        if (xDirection < 0 && radarSpeedMeasureEnd == null) {
            if (position.x < radarSpeedStartX && radarSpeedMeasureStarted == null) {
                radarSpeedMeasureStarted = LocalDateTime.now();
            }
            if (position.x < radarSpeedEndX) {
                radarSpeedMeasureEnd = LocalDateTime.now();
            }
        }
    }

    public void changeMaxSpeed(int newMaxSpeed) {
        synchronized (maxSpeed) {
            maxSpeed = newMaxSpeed;
        }
    }

    private int calculateSpeed(int direction, double accelerationValue, int currentSpeed) {
        if (direction == 0) {
            return 0;
        }
        double res = currentSpeed + direction * accelerationValue;
        if (accelerationValue > 0 && (int)res > maxSpeed) {
            return maxSpeed;
        } else if (accelerationValue < 0 && (int)res <= 0) {
            return 0;
        }
        if ((res < 0 && direction == 1) || (res > 0 && direction == -1)) {
            return 0;
        }
        return (int)res;
    }

    public void carRemoveLogic() {
        this.carLeftTheStageTime = LocalDateTime.now();
        this.endPos = new Point(position.x, position.y);
    }

    public Point getPosition() {
        return new Point((int)getX(), (int)getY());
    }

    public long getAverageSpeed() {
        return allSpeedsMeasured / allMovesCount;
    }

    public int getMaxSpeedReached() {
        return maxSpeedReached;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public LocalDateTime getCarEnterOnStageTime() {
        return carEnterOnStageTime;
    }

    public LocalDateTime getCarLeftTheStageTime() {
        return carLeftTheStageTime;
    }

    public Point getBeginPos() {
        return beginPos;
    }

    public Point getEndPos() {
        return endPos;
    }
}
