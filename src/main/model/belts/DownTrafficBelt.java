package main.model.belts;

import java.awt.*;
import java.util.*;
import java.util.List;

import javafx.scene.image.ImageView;
import main.model.Car;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.DirectionEnum;

/**
 * Created by Krzysztof Baran
 * Obiekt reprezentujący pas na którym samochody poruszają się w dół, dziedziczy po pasie abstrakcyjnym
 */
public class DownTrafficBelt extends TrafficBelt {
    /**
     * Konstruktor pasa, inicjalizacja podstawowych parametrów oraz ustawienie pozycji startu i końca pasa
     * @param beltNumber Numer pasa
     * @param carsLimit Limit samochodów na zadanym pasie
     * @param xPos Wartość na osi X lewego górnego rogu pasa
     * @param yPos Wartość na osi Y lewego górnego rogu pasa
     * @param width Szerokość pasa (oś X)
     * @param height Wysokość pasa (oś Y)
     * @param speedControlXStart Początek pomiaru odcinkowego na osi X
     * @param speedControlXEnd Koniec pomiaru odcinkowego na osi X
     */
    public DownTrafficBelt(int beltNumber, int carsLimit, int xPos, int yPos, int width, int height, Integer speedControlXStart,
                           Integer speedControlXEnd) {
        super(beltNumber, carsLimit, xPos, yPos, width, height, DirectionEnum.DOWN, speedControlXStart, speedControlXEnd);
        beltXStart = beltXEnd = xPos + 3;
        beltYStart = yPos;
        beltYEnd = yPos + height;
    }

    /**
     * Sprawdzenie kolizji pomiędzy dwoma samochodami z przemieszczeniem tego pierwszego
     * @param car Samochód 1
     * @param c Samochód 2
     * @return True jeśli zajdzie kolizja, wpp false
     */
    @Override
    protected boolean collisionBetweenTwoCars(Car car, Car c) {
        return car.getPosition().y + BELT_HEIGHT >= c.getPosition().y - BELT_HEIGHT * getStoppingDistFact() &&
            car.getPosition().y < c.getPosition().y;
    }

    /**
     * Sprawdzenie, czy samochód ma jeszcze dużą odległość od pasa (maxSpeed * 3, czyli na 3 kolejne ruchy)
     * @param car Aktualnie analizowany samochód
     * @param nextCrossing Najbliższe skrzyżowanie do którego się zbliża
     * @return True, jeśli ma odpowiednią odległość, wpp false
     */
    @Override
    protected boolean hasDistanceToCrossing(Car car, TrafficLightsAndCrossing nextCrossing) {
        return car.getPosition().y < nextCrossing.getY1() - Math.abs(car.getMaxSpeed()) * 3;
    }

    /**
     * Sprawdzenie, czy samochód ma odpowiednie dla kierunku zielone światło
     * @param nextCrossing Najbliższe skrzyżowanie do którego się zbliża
     * @return True, jeśli jest zielone, wpp false
     */
    @Override
    protected synchronized boolean hasGreenLight(TrafficLightsAndCrossing nextCrossing) {
        //no more crossings to pass
        if (nextCrossing == null) {
            return true;
        }
        return nextCrossing.isVerticalGreen();
    }

    /**
     * Znalezienie następnego skrzyżowania odpowiadającego kierunkowi jazdy samochodu
     * @param carPos Pozycja samochodu
     * @return Następne skrzyżowanie. Jeśli brak, to null
     */
    @Override
    protected TrafficLightsAndCrossing getNextCrossingAndLights(Point carPos) {
        TrafficLightsAndCrossing res = null;
        for (TrafficLightsAndCrossing crossing : crossingAndLights) {
            if (crossing.getY1() > carPos.y + BELT_HEIGHT && (res == null || res.getY1() > crossing.getY1())) {
                res = crossing;
            }
        }
        return res;
    }

    /**
     * Metoda znajdująca wszystkie samochody dla danego pasa, które opuściły już scenę.
     * Wołana jest funkcja clear, która usuwa je z listy na pasie i woła logikę specyficzną dla samochodu.
     * Następnie zwracana jest lista samochodów do usunięcia ze sceny poprzez kontroler
     * @return Lista samochodów do usunięcia ze sceny poprzez kontroler
     */
    @Override
    protected synchronized List<ImageView> cleanup() {
        List<ImageView> carsViewsToRemove = new ArrayList<>();
        List<Car> carsToRemove = new ArrayList<>();
        containingCars.stream().filter(car -> car.getY() >= beltYEnd).forEach(car -> {
            carsViewsToRemove.add(car);
            carsToRemove.add(car);
            car.carRemoveLogic();
        });
        clear(carsToRemove);
        return carsViewsToRemove;
    }
}
