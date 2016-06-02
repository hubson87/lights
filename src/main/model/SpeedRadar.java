package main.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SpeedRadar extends ImageView{

    public SpeedRadar(double x, double y) {
        super();
        Image img = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/radar.png"));
        setImage(img);
        double ratio = 16;
        setFitWidth(img.getWidth()/ratio);
        setFitHeight(img.getWidth()/ratio);
        setX(x - getFitWidth() / 2.0);
        setY(y);
    }

}
