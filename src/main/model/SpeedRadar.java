package main.model;

import java.awt.Point;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SpeedRadar extends ImageView{
    private final Point position;

    public SpeedRadar(Point position) {
        super();
        this.position = position;
        Image img = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/radar.png"));
        setImage(img);
        double ratio = 8.0;
        setFitWidth(img.getWidth()/ratio);
        setFitHeight(img.getWidth()/ratio);
        setX(position.getX());
        setY(position.getY());
    }

    public Point getPosition() {
        return position;
    }
}
