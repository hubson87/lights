package main.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SpeedLimitSign extends ImageView {

    public SpeedLimitSign(double x, double y) {
        super();
        Image img = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/maxSpeed.PNG"));
        setImage(img);
        double ratio = 3;
        setFitWidth(img.getWidth()/ratio);
        setFitHeight(img.getWidth()/ratio);
        setX(x - getFitWidth() / 2.0);
        setY(y);
    }
}
