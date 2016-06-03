package main.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by Krzysztof Baran
 * Klasa reprezentująca graficzny obiekt początku/końca pomiaru odcinkowego
 */
public class SpeedRadar extends ImageView{

    /**
     * Konstruktor ustawiający pozycję i rozmiar radarów na scenie
     * @param x Środek obrazu na osi X
     * @param y Góra obrazu na osi Y
     */
    public SpeedRadar(double x, double y) {
        //utworzenie klasy nadrzędnej
        super();
        //Pobranie i ustawienie obrazu z resource'ów
        Image img = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/radar.png"));
        setImage(img);
        //Współczynnik pomniejszenia obrazu
        double ratio = 16;
        //Przeliczenie i ustawienie rozmiarów obrazu w wysokości i szerokości
        setFitWidth(img.getWidth()/ratio);
        setFitHeight(img.getWidth()/ratio);
        //Przesunięcie obrazu względem osi X o połowę rozmiaru
        setX(x - getFitWidth() / 2.0);
        //Ustawienie wartości na osi Y
        setY(y);
    }

}
