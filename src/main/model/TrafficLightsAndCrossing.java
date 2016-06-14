package main.model;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Krzysztof Baran
 * Klasa reprezentująca obiekt skrzyżowania oraz umieszczonych na nim świateł
 */
public class TrafficLightsAndCrossing {
    /**
     * Rozmiar poziomy obrazka ze światłami (użyty do wycentrowania pozycji)
     */
    private static final int LIGHTS_IMAGE_SIZE_X = 14;
    /**
     * Rozmiar pionowy obrazka ze światłami (użyty do wycentrowania pozycji)
     */
    private static final int LIGHTS_IMAGE_SIZE_Y = 14;
    /**
     * Położenie skrzyżowania na scenie -> wierzchołki reprezentującego je prostokątu
     */
    private final int x1, x2, y1, y2;
    /**
     * Obraz świateł czerwone/żółte/zielone
     */
    private final Image redLight, yellowLight, greenLight;
    /**
     * Panel reprezentujący skrzyżowanie
     */
    private final Pane crossingCoordinates;
    /**
     * Objekt przetrzymujący obraz świateł pionowych (góra/dół)
     */
    private ImageView lightUp, lightDown;
    /**
     * Objekt przetrzymujący obraz świateł poziomych (lewo/prawo)
     */
    private ImageView lightLeft, lightRight;
    /**
     * Flaga mówiąca o tym, czy aktualnie poziome światła ustawione są jako zielone i poziome pasy są przejezdne
     */
    private boolean horizontalGreen;
    /**
     * Flaga mówiąca o tym, czy aktualnie pionowe światła ustawione są jako zielone i pionowe pasy są przejezdne
     */
    private boolean verticalGreen;
    /**
     * Lista samochodów aktualnie znajdujących się na skrzyżowaniu
     */
    private List<Car> containingCars;
    /**
     * Ilość pasów na skrzyżowaniu w pionie
     */
    private int vBeltsCount;

    /**
     * Konstruktor ustawiający położenie skrzyżowania oraz pobierający wszystkie potrzebne pliki graficzne.
     * Ustawia również pionowe światła jako czerwone oraz poziome światła jako zielone.
     *
     * @param x1 Wartość osi X dla początkowego brzegu skrzyżowania
     * @param x2 Wartość osi X dla końcowego brzegu skrzyżowania
     * @param y1 Wartość osi X dla początkowego brzegu skrzyżowania
     * @param y2 Wartość osi Y dla końcowego brzegu skrzyżowania
     * @param vBeltsCount Ilość pasów pionowych na skrzyżowaniu
     */
    public TrafficLightsAndCrossing(int x1, int x2, int y1, int y2, int vBeltsCount) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        containingCars = new ArrayList<>();
        //inicjalizowanie panelu skrzyżowania
        this.crossingCoordinates = initPane(x1, x2, y1, y2);
        //pobranie wszystkich resourców potrzebnych do jego wyświetlenia
        this.redLight = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/red.png"));
        this.yellowLight = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/yellow.png"));
        this.greenLight = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/green.png"));
        //ustawienie pionowych świateł jako czerwonych, a poziomych jako zielonych
        setLights(redLight, false, greenLight, true);
        this.vBeltsCount = vBeltsCount;
    }

    /**
     * Funkcja pomocnicza konstruktora, mająca za zadanie ustawić wszystkie wartości panelu reprezentującego skrzyżowanie.
     * Ustawiany jest odpowiedni rozmiar na scenie w zależności od wierzchołków, a także ciemne tło imitujące asfalt.
     * @param x1 Wartość osi X dla początkowego brzegu skrzyżowania
     * @param x2 Wartość osi X dla końcowego brzegu skrzyżowania
     * @param y1 Wartość osi X dla początkowego brzegu skrzyżowania
     * @param y2 Wartość osi Y dla końcowego brzegu skrzyżowania
     * @return Panel graficzny reprezentujący skrzyżowanie, do umieszczenia na scenie
     */
    private Pane initPane(int x1, int x2, int y1, int y2) {
        Pane res = new Pane();
        //Ustawienie rozmiaru panelu
        res.setPrefSize(x2 - x1, y2 - y1);
        res.setMinSize(x2 - x1, y2 - y1);
        res.setMaxSize(x2 - x1, y2 - y1);
        //Przeniesienie go na scenie w początkowy górny lewy wierzchołek
        res.relocate(x1, y1);
        //Ustawienie koloru tła
        res.setStyle("-fx-background-color: #34495e;");
        return res;
    }

    /**
     * Funkcja pomocnicza, synchronizowana na potrzeby wątków, mająca za zadanie ustawić odpowiednie wartości dla zadanych świateł.
     * Zarówno graficzne, jak i logiczne w kodzie.
     * Ustawiane są odpowiednio flagi pozwalające samochodom jechać lub nie, a następnie usuwane są wszystkie obiekty graficzne z panelu
     * skrzyżowania, po czym umieszczane są odpowiednie obrazy dla świateł przekazane w parametrach.
     * @param vertical Obraz dla świateł pionowych
     * @param verticalGreen Wartość flagi mówiącej o tym, czy samochody mogą jechać w pionie
     * @param horizontal Obraz dla świateł poziomych
     * @param horizontalGreen Wartość flagi mówiącej o tym, czy samochody mogą jechać w poziomie
     */
    private synchronized void setLights(Image vertical, boolean verticalGreen, Image horizontal, boolean horizontalGreen) {
        //Usunięcie wszystkich elementów graficznych z panelu skrzyżowania
        crossingCoordinates.getChildren().clear();
        //Ustawienie obrazków dla świateł pionowych/poziomych
        setVerticalLights(vertical);
        setHorizontalLights(horizontal);
        //Dodanie ustawionych świateł na scenie
        crossingCoordinates.getChildren().addAll(lightLeft, lightUp, lightRight, lightDown);
        //Ustawienie flag mówiących, czy samochód może jechać w pionie/poziomie
        this.verticalGreen = verticalGreen;
        this.horizontalGreen = horizontalGreen;
    }

    /**
     * Funkcja przeliczająca pozycję świateł pionowych na scenie oraz ustawiająca odpowiedni obrazek w zmiennej je reprezentującej
     * @param img Obrazek reprezentujący odpowiednie światło do ustawienia w pionie
     */
    private void setVerticalLights(Image img) {
        this.lightUp = new ImageView(img);
        this.lightUp.relocate((x2 - x1) / 2 - LIGHTS_IMAGE_SIZE_X / 2, 0);
        this.lightDown = new ImageView(img);
        this.lightDown.relocate((x2 - x1) / 2 - LIGHTS_IMAGE_SIZE_X / 2, y2 - y1 - LIGHTS_IMAGE_SIZE_Y);
    }

    /**
     * Funkcja przeliczająca pozycję świateł poziomych na scenie oraz ustawiająca odpowiedni obrazek w zmiennej je reprezentującej
     * @param img Obrazek reprezentujący odpowiednie światło do ustawienia w pionie
     */
    private void setHorizontalLights(Image img) {
        this.lightLeft = new ImageView(img);
        this.lightLeft.relocate(0, (y2 - y1) / 2 - LIGHTS_IMAGE_SIZE_Y / 2);
        this.lightRight = new ImageView(img);
        this.lightRight.relocate(x2 - x1 - LIGHTS_IMAGE_SIZE_X, (y2 - y1) / 2 - LIGHTS_IMAGE_SIZE_Y / 2);
    }

    /**
     * Funkcja zmieniająca światła, wołająca wszystkie odpowiednie funkcje pomocnicze.
     * Platform.runLater gwarantuje, że światła na panelu zostaną zmienione za pomocą wątku UI
     * Jeśli aktualnie ustawione są zielone światła w poziomie, to blokowane zostają samochody w poziomie, a światło zostaje zmienione na żółte.
     * Następnie w odrębnym wątku, aby nie blokować aktualnego, po jednej sekundzie, światło poziome zostaje ustawione jako czerwone, a
     * pionowe zmieniane jest na zielone.
     * Analogicznie jeśli ustawione aktualnie jest światło zielone w pionie.
     */
    public void changeLights() {
        new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(() -> {
                    if (horizontalGreen) {
                        setLights(redLight, false, yellowLight, false);
                        switchLightsInThread(greenLight, redLight, true);
                    } else {
                        setLights(yellowLight, false, redLight, false);
                        switchLightsInThread(redLight, greenLight, false);
                    }
                });
                return null;
            }
        }).start();
    }

    /**
     * Metoda pomocnicza, której zadaniem jest zmiana świateł w nowym wątku.
     * Nowy wątek usypiany jest na jedną sekundę, tak aby żółte światło mogło poświecić przez ten czas na scenie, a następnie przejść w czerwone
     * @param vertical Obrazek reprezentujący nowe światło w pionie
     * @param horizontal Obrazek reprezentujący nowe światło w poziomie
     * @param newVerticalGreenValue Nowa wartość dla światła zielonego w pionie. Jeśli false, to ustawiane jest światło w poziomie jako zielone
     */
    private synchronized void switchLightsInThread(Image vertical, Image horizontal, boolean newVerticalGreenValue) {
        new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Platform.runLater(() -> setLights(vertical, newVerticalGreenValue, horizontal, !newVerticalGreenValue));
                }
                return null;
            }
        }).start();
    }

    /**
     * Getter dla panelu reprezentującego położenie skrzyżowania
     * @return Panel reprezentujący położenie skrzyżowania
     */
    public Pane getCrossingGraphics() {
        return crossingCoordinates;
    }

    /**
     * Getter mówiący o tym, czy poziome światło jest ustawione jako zielone
     * @return True, jeśli poziome światło jest ustawione jako zielone
     */
    public synchronized boolean isHorizontalGreen() {
        return horizontalGreen;
    }
    /**
     * Getter mówiący o tym, czy pionowe światło jest ustawione jako zielone
     * @return True, jeśli pionowe światło jest ustawione jako zielone
     */
    public synchronized boolean isVerticalGreen() {
        return verticalGreen;
    }

    /**
     * Początek skrzyżowania na osi X
     * @return Początek skrzyżowania na osi X
     */
    public int getX1() {
        return x1;
    }

    /**
     * Koniec skrzyżowania na osi X
     * @return Koniec skrzyżowania na osi X
     */
    public int getX2() {
        return x2;
    }

    /**
     * Początek skrzyżowania na osi Y
     * @return Początek skrzyżowania na osi Y
     */
    public int getY1() {
        return y1;
    }

    /**
     * Koniec skrzyżowania na osi Y
     * @return Koniec skrzyżowania na osi Y
     */
    public int getY2() {
        return y2;
    }

    /**
     * Metoda sprawdzająca, czy dany samochód jest na liście samochodów na skrzyżowaniu
     * @param car Analizowany samochód
     * @return Jeśli samochód jest na liście => true, wpp. false
     */
    private boolean containsCar(Car car) {
        return containingCars.contains(car);
    }

    /**
     * Metoda sprawdzająca, czy dany samochód znajduje się na skrzyżowaniu
     */
    public synchronized boolean isCarOnTheCrossing(Point carPos, int beltHeight) {
        return ((carPos.x >= getX1() && carPos.x <= getX2()) ||
                (carPos.x + beltHeight >= getX1() && carPos.x + beltHeight <= getX2()))
                && ((carPos.y >= getY1() && carPos.y <= getY2()) ||
                (carPos.y + beltHeight  >= getY1() && carPos.y + beltHeight <= getY2()));
    }

    /**
     * Metoda dodająca samochód do skrzyżowania jeśli jeszcze go nie ma
     * @param car Analizowany samochód
     */
    public synchronized void addCarIfNotExists(Car car) {
        if (!containsCar(car)) {
            containingCars.add(car);
        }
    }

    /**
     * Metoda usuwająca samochód ze skrzyżowania jeśli istnieje na liście
     * @param car Analizowany samochód
     */
    public synchronized void removeCar(Car car) {
        if (containsCar(car)) {
            containingCars.remove(car);
        }
    }

    /**
     * Getter dla listy samochodów na skrzyżowaniu
     * @return Samochody znajdujące się na skrzyżowaniu
     */
    public List<Car> getContainingCars() {
        return containingCars;
    }

    /**
     * Getter dla ilości pasów pionowych na światłach
     * @return Ilość pasów pionowych na światłach
     */
    public int getVBeltsCount() {
        return vBeltsCount;
    }
}
