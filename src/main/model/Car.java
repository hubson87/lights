package main.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import main.model.belts.TrafficBelt;
import main.model.enums.DirectionEnum;
import main.model.enums.WeatherEnum;
import main.utils.DateUtils;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * Created by Krzysztof Baran
 * Obiekt reprezentujący samochód oraz wszelkie operacje z nim związane (przyspieszanie, obliczanie pozycji itd)
 * Dziedziczy po ImageView przez co sam w sobie jest obiektem sceny zawierającym obraz wewnątrz
 */
public class Car extends ImageView {
    /**
     * Generator liczb losowych aby niezależnie zmieniać przyspieszenie samochodów z elementem losowości
     */
    private final static Random RANDOM = new Random();
    /**
     * Pozycja początkowa pojazdu na scenie.
     * Służy również do obliczenia całkowitej średniej prędkości poruszania się pojazdu
     */
    private final Point beginPos;
    /**
     * Czas i data pojawienia się pojazdu na scenie.
     * Służy również do obliczenia całkowitej średniej prędkości poruszania się pojazdu
     */
    private final LocalDateTime carEnterOnStageTime;
    /**
     * Pozycja końcowa pojazdu podczas zdejmowania go ze sceny.
     * Służy również do obliczenia całkowitej średniej prędkości poruszania się pojazdu
     */
    private Point endPos;
    /**
     * Czas i data zdjęcia pojazdu ze sceny.
     * Służy również do obliczenia całkowitej średniej prędkości poruszania się pojazdu
     */
    private LocalDateTime carLeftTheStageTime;
    /**
     * Maksymalna prędkość jaką może osiągnąć samochód
     */
    private Integer maxSpeed;
    /**
     * Aktualna prędkość samochodu
     */
    private int speed;
    /**
     * Aktualne przyspieszenie samochodu (gdy dodatnie -> porusza się w prawo lub dół, wpp w lewo lub górę)
     */
    private double acceleration;
    /**
     * Aktualna pozycja samochodu
     */
    private Point position;
    /**
     * Kierunek poruszania się pojazdu po osi X oraz Y. Jeśli xDirection = 1 => prawo, jeśli xDirection = -1 => lewo,
     * jeśli yDirection = 1 => dół, jeśli yDirection = -1 => góra
     */
    private int xDirection, yDirection;
    /**
     * Data rozpoczęcia pomiaru odcinkowego jeśli takowy istnieje na pasie
     */
    private LocalDateTime radarSpeedMeasureStarted;
    /**
     * Data zakończenia pomiaru odcinkowego jeśli takowy istnieje na pasie
     */
    private LocalDateTime radarSpeedMeasureEnd;
    /**
     * Pozycja w której rozpoczyna się i kończy pomiar odcinkowy jeśli takowy istnieje na pasie
     */
    private Integer radarSpeedStartX, radarSpeedEndX;
    /**
     * Aktuanie panująca pogoda na scenie.
     * Służy do gromadzenia statystyk w jakiej pogodzie jaka była prędkość średnia pojazdu
     */
    private WeatherEnum currentWeather;
    /**
     * Data i czas kiedy następowała ostatnia zmiana pogody.
     * Używana do przeliczeń średniej prędkości w obecnej pogodzie
     */
    private LocalDateTime lastWeatherChangeTime;
    /**
     * Pozycja samochodu podczas ostatniej zmiany pogody.
     * Używana do przeliczeń średniej prędkości w obecnej pogodzie
     */
    private Point lastWeatherPosition;
    /**
     * Statystyki prędkości średnich samochodu z podziałem na warunki pogodowe panujące na scenie
     */
    private Map<WeatherEnum, List<Long>> speedsForWeather;
    /**
     * Flaga mówiąca o tym, czy samochód wziął udział w kolizji
     */
    private boolean carIsInCollision;
    /**
     * Pogoda w której kolizja nastąpiła
     */
    private WeatherEnum collisionWeather;

    /**
     * Konstruktor obiektu samochodu
     *
     * @param maxSpeed         Maksymalna prędkość jaką samochód może uzyskać (może ulec zimanie w zależności od pogody)
     * @param direction        Kierunek poruszania się pojazdu
     * @param beltXPos         Współrzędna X rozpoczęcia się pasu (pozycja na osi X z której startuje samochód)
     * @param beltYPos         Współrzędna Y rozpoczęcia się pasu (pozycja na osi Y z której startuje samochód)
     * @param radarSpeedStartX Współrzędna X rozpoczęcia pomiaru odcinkowego na pasie, jeśli takowy istnieje
     * @param radarSpeedEndX   Współrzędna X zakończenia pomiaru odcinkowego na pasie, jeśli takowy istnieje
     * @param weatherEnum      Początkowa pogoda panująca na scenie
     */
    public Car(int maxSpeed, DirectionEnum direction, int beltXPos, int beltYPos, Integer radarSpeedStartX, Integer radarSpeedEndX, WeatherEnum weatherEnum) {
        super();
        if (direction == null) {
            throw new IllegalArgumentException("Direction must be defined");
        }
        //Pobranie obrazu z resource'ów oraz ustawienie jego rozmiaru
        loadImgAndResize(beltXPos, beltYPos);
        //Ustawienie kierunków poruszania się pojazdu
        xDirection = direction == DirectionEnum.RIGHT ? 1 : (direction == DirectionEnum.LEFT ? -1 : 0);
        yDirection = direction == DirectionEnum.DOWN ? 1 : (direction == DirectionEnum.UP ? -1 : 0);
        //Ustawienie aktualnej pozycji oraz pozycji początkowej pojazdu
        this.position = new Point(beltXPos, beltYPos);
        this.beginPos = new Point(beltXPos, beltYPos);
        //Ustawienie daty pojawienia się samochodu na scenie
        this.carEnterOnStageTime = LocalDateTime.now();
        //Ustawienie aktualnej prędkości maksymalnej pojazdu (w zależności od pogody)
        this.maxSpeed = maxSpeed;
        //Ustawienie prędkości początkowej pojazdu wjeżdżającego na scenę
        this.speed = maxSpeed / 4;
        //Ustawienie przyspieszenia pojazdu w zależności od osiąganej prędkości maksymalnej
        this.acceleration = (double) (maxSpeed + RANDOM.nextInt(30)) / 10.0;
        //Ustawienie początku i końca pomiaru odcinkowego prędkości na pasie
        this.radarSpeedStartX = radarSpeedStartX;
        this.radarSpeedEndX = radarSpeedEndX;
        //Ustawienie aktualnej pogody
        this.currentWeather = weatherEnum;
        //Ustawienie ostatniej zmiany pogody na aktualny czas
        this.lastWeatherChangeTime = LocalDateTime.now();
        //Ustawienie ostatniej pozycji pojazdy podczas zmiany pogody na aktualną
        this.lastWeatherPosition = new Point(beltXPos, beltYPos);
        //Zainicjowanie statystyk prędkości względem pogody
        this.speedsForWeather = new HashMap<>();
        //Oznaczenie braku kolizji na początku
        this.carIsInCollision = false;
        this.collisionWeather = null;
    }

    /**
     * Inicjalizacji tymczasowego obiektu samochodu do przeliczeń kolizji
     * @param x Pozycja pojazdu na osi X
     * @param y Pozycja pojazdu na osi Y
     */
    public Car(double x, double y) {
        this.setX(x);
        this.setY(y);
        beginPos = null;
        carEnterOnStageTime = null;
    }

    /**
     * Inicjalizacji tymczasowego obiektu samochodu do przeliczeń kolizji
     * @param c Aktualnie analizowany samochód
     */
    public Car(Car c) {
        this.setX(c.getX());
        this.setY(c.getY());
        this.position = new Point(c.position.x, c.position.y);
        this.xDirection = c.xDirection;
        this.yDirection = c.yDirection;
        beginPos = null;
        carEnterOnStageTime = null;
        this.maxSpeed = c.maxSpeed;
        this.speed = c.speed;
        this.acceleration = c.acceleration;
    }

    /**
     * Wczytanie obiektu graficznego pojazdu i ustawienie jego pozycji na scenie
     * @param startPosX Współrzędna startowa na osi X
     * @param startPosY Współrzędna startowa na osi Y
     */
    private void loadImgAndResize(int startPosX, int startPosY) {
        //Wczytanie obrazu z resource'ów
        Image carImg = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/car.png"));
        setImage(carImg);
        //Ustawienie maksymalnej wielkości obrazu
        int maxSize = TrafficBelt.BELT_HEIGHT - 2;
        //Pobranie większego wymiaru obrazu i obliczenie współczynnika, przez jaki trzeba przemnożyć obie wartości,
        //aby większa z nich była rozmiaru maxSize
        double scale = ((double) maxSize) / (carImg.getHeight() > carImg.getWidth() ? carImg.getHeight() : carImg.getWidth());
        //Przeliczenie i ustawienie rozmiaru obrazu
        setPreserveRatio(true);
        setFitWidth(carImg.getWidth() * scale);
        setFitHeight(carImg.getHeight() * scale);
        //Ustawienie pozycji początkowej pojazdu
        setX(startPosX);
        setY(startPosY);
    }

    /**
     * Metoda mająca przyspieszyć samochód lub jeśli osiągnął prędkość maksymalną, to ustrzymanie jej.
     * Przelicza także nową pozycję na ekranie po danej iteracji symulacji
     */
    public void go() {
        synchronized (maxSpeed) {
            Point newPos = calculatePosition(acceleration);
            setX(newPos.getX());
            setY(newPos.getY());
        }
    }

    /**
     * Metoda mająca zwolnić samochód lub jeśli osiągnął 0, to ustrzymanie jej bez zmian.
     * Przelicza także nową pozycję na ekranie po danej iteracji symulacji.
     * Prędkość hamowania wynosi 5*przypieszenie
     */
    public void stop() {
        synchronized (maxSpeed) {
            Point newPos = calculatePosition(-acceleration * 5);
            setX(newPos.getX());
            setY(newPos.getY());
        }
    }

    /**
     * Funkcja pomocnicza, której zadaniem jest przeliczenie nowej pozycji dla zadanej wartości przypieszenia
     * Oblicza również aktualną prędkość oraz sprawdza i ustawia wartości związane z pomiarem odcinkowym
     * @param accelerationValue Aktualne przyspieszenie
     * @return Nowa pozycja pojazdu
     */
    private Point calculatePosition(double accelerationValue) {
        //Obliczenie prędkości po osi X i Y
        int xSpeed = calculateSpeed(xDirection, accelerationValue, speed);
        int ySpeed = calculateSpeed(yDirection, accelerationValue, speed);
        //Ustawienie prędkości
        speed = xSpeed != 0 ? xSpeed : ySpeed;
        //Zmiana pozycji samochodu
        position.x += xSpeed;
        position.y += ySpeed;
        //Ustawienie wartości pomiaru odcinkowego, jeśli się rozpoczął lub skończył
        checkAndMarkRadars(position);
        return position;
    }

    /**
     * Ustawienie wartości związanych z pomiarem odcinkowym, jeśli się zaczął, lub skończył
     * @param position Aktualna pozycja samochodu
     */
    private void checkAndMarkRadars(Point position) {
        if (xDirection == 0 || radarSpeedStartX == null) {
            return;
        }
        //Jeśli samochód porusza się w prawo i nie zakończył się jeszcze pomiar prędkości
        if (xDirection > 0 && radarSpeedMeasureEnd == null) {
            //Jeśli pozycja X przekroczyła pozycję rozpoczęcia pomiaru, a ten nie jest uwzględniony, oznacza to
            //rozpoczęcie pomiaru, więc zapisujemy datę i czas rozpoczęcia pomiaru
            if (position.x > radarSpeedStartX && radarSpeedMeasureStarted == null) {
                radarSpeedMeasureStarted = LocalDateTime.now();
            }
            //Jeśli pozycja X przekroczyła pozycję zakończenia pomiaru, zapisujemy datę i czas zakończenia pomiaru
            if (position.x > radarSpeedEndX) {
                radarSpeedMeasureEnd = LocalDateTime.now();
            }
        }

        //Jeśli samochód porusza się w lewo i nie zakończył się jeszcze pomiar prędkości
        if (xDirection < 0 && radarSpeedMeasureEnd == null) {
            //Jeśli pozycja X przekroczyła pozycję rozpoczęcia pomiaru, a ten nie jest uwzględniony, oznacza to
            //rozpoczęcie pomiaru, więc zapisujemy datę i czas rozpoczęcia pomiaru
            if (position.x < radarSpeedStartX && radarSpeedMeasureStarted == null) {
                radarSpeedMeasureStarted = LocalDateTime.now();
            }
            //Jeśli pozycja X przekroczyła pozycję zakończenia pomiaru, zapisujemy datę i czas zakończenia pomiaru
            if (position.x < radarSpeedEndX) {
                radarSpeedMeasureEnd = LocalDateTime.now();
            }
        }
    }

    /**
     * Zmiana maksymalnej prędkości samochodu, która zaszła poprzez zmianę pogody.
     * Używa funkcji pomocniczej do zapisania wszystkich potrzebnych statystyk z tym związanych
     * @param newMaxSpeed Nowa prędkość maksymalna
     * @param newWeather Nowe warunki pogodowe
     */
    public void changeMaxSpeed(int newMaxSpeed, WeatherEnum newWeather) {
        synchronized (maxSpeed) {
            maxSpeed = newMaxSpeed;
            updateSpeedForTheWeather(newWeather);
        }
    }

    /**
     * Funkcja pomocnicza zapisująca wszystkie statystyki związane z ostatnią pogodą i ustawia nową
     * @param newWeather Nowe warunki pogodowe
     */
    private synchronized void updateSpeedForTheWeather(WeatherEnum newWeather) {
        //Pobranie koordynatów ostatniej zmiany pogody (w zależności od osi)
        int startCoordinate = xDirection == 0 ? lastWeatherPosition.y : lastWeatherPosition.x;
        //Pobranie koordynatów z aktualnej pozyji samochodu (w zależności od osi)
        int endCoordinate = xDirection == 0 ? position.y : position.x;
        //Pobranie aktualnej daty
        LocalDateTime nowDateTime = LocalDateTime.now();
        //Obliczenie prędkości na odcinku -> pozycja podczas ostatniej zmiany pogody - aktualna pozycja
        //w czasie obecznym - czas ostatniej zmiany pogody
        long speed = calculateSpeedStats(startCoordinate, endCoordinate, lastWeatherChangeTime, nowDateTime);
        //Jeśli obecnej pogody nie ma jeszcze w statystykach, to ją inicjujemy
        if (!speedsForWeather.containsKey(currentWeather)) {
            speedsForWeather.put(currentWeather, new ArrayList<>());
        }
        //Dopisujemy obliczoną prędkość dla zadanych warunków pogodowych
        speedsForWeather.get(currentWeather).add(speed);
        //Ustawienie ostatniego czasu zmiany pogody na aktualny
        lastWeatherChangeTime = nowDateTime;
        //Ustawienie pozycji podczas ostatniej zmiany pogody na aktualną
        lastWeatherPosition = new Point(position.x, position.y);
        //Ustawienie nowej pogody
        currentWeather = newWeather;
    }

    /**
     * Przeliczenie nowej wartości prędkości dla samochodu
     * @param direction Kierunek poruszania się pojazdu
     * @param accelerationValue Wartość obecnego przyspieszenia (dodatnie => gaz, ujemne => hamulec)
     * @param currentSpeed Obecna prędkość
     * @return Nowa wartość prędkości pojazdu
     */
    private int calculateSpeed(int direction, double accelerationValue, int currentSpeed) {
        if (direction == 0) {
            return 0;
        }
        //Obliczenie nowej prędkości = obecna prędkość + kierunek na osi * prędkość przyspieszenia
        double res = currentSpeed + direction * accelerationValue;
        //Jeśli przyspieszenie jest dodatnie i przekroczono maksymalną prędkość => zwracamy maksymalną prędkość
        //Jeśli przyspieszenie jest ujemne i prędkość mniejsza = 0 => ustawiamy 0
        if (accelerationValue > 0 && (int) res > maxSpeed) {
            return maxSpeed;
        } else if (accelerationValue < 0 && (int) res <= 0) {
            return 0;
        }
        //Jeśli prędkość mniejsza od 0 i kierunek dodatni lub prędkość większa 0 i kierunek ujemny, to zwracamy 0,
        //ponieważ pojazd zahamował
        if ((res < 0 && direction == 1) || (res > 0 && direction == -1)) {
            return 0;
        }
        return (int) res;
    }

    /**
     * Logika wywoływana przy usuwaniu pojazdu ze sceny
     * Ustawian jest data usunięcia pojazdu oraz jego pozycja podczas usuwania go ze sceny do obliczeń
     * Aktualizowane są również dane o ostatniej pogodzie i prędkościach podczas niej
     */
    public void carRemoveLogic() {
        this.carLeftTheStageTime = LocalDateTime.now();
        this.endPos = new Point(position.x, position.y);
        updateSpeedForTheWeather(currentWeather);
    }

    /**
     * Getter dla pozycji pojazdu
     * @return Aktualna pozycja pojazdu
     */
    public Point getPosition() {
        return new Point((int) getX(), (int) getY());
    }

    /**
     * Metoda obliczająca średnią prędkość pojadu na całym odcinku trasy.
     * Pobieramy koordynaty początkowe, końcowe i wołamy metodę obliczania prędkości dla nich
     * oraz czasu pojawienia się i zniknięcia pojazdy ze sceny
     * @return Średnia prędkość pojazdy na całym odcinku trasy
     */
    public long getAverageSpeed() {
        int startCoordinate = xDirection == 0 ? beginPos.y : beginPos.x;
        int endCoordinate = xDirection == 0 ? endPos.y : endPos.x;
        return calculateSpeedStats(startCoordinate, endCoordinate, carEnterOnStageTime, carLeftTheStageTime);
    }

    /**
     * Obliczenie prędkości dla pomiaru odcinkowego, jeśli tylko takowy istniał.
     * Jeśli brak punktów pomiaru, zwracany jest 'n/a'.
     * Obliczana jest prędkość na podstawie odcinka pozycji startu i końca pomiaru oraz czasów ich przekroczenia
     * @return 'n/a' jeśli brak pomiaru, jeśli pomiar istniał, to średnia prędkość podczas pomiaru
     */
    public String getRadarMeasuredSpeed() {
        if (radarSpeedStartX == null || radarSpeedEndX == null) {
            return "n/a";
        }
        return String.valueOf(calculateSpeedStats(radarSpeedStartX, radarSpeedEndX, radarSpeedMeasureStarted, radarSpeedMeasureEnd));
    }

    /**
     * Obliczenie średniej prędkości na zadanym odcinku drogowym w zadanym czasie na potrzeby statystyk
     * Ustalone zostały 25 pikseli => 4 km. 0.5s symulacji to 1 godzina
     * @param position1 Pozycja początkowa pojazdu
     * @param position2 Pozycja końcowa pojazdu
     * @param timeStart Czas rozpoczęcia pomiaru prędkości
     * @param timeEnd Czas zakończenia pomiaru prędkości
     * @return Obliczona średnia prędkość dla danych parametrów
     */
    private long calculateSpeedStats(double position1, double position2, LocalDateTime timeStart, LocalDateTime timeEnd) {
        double distance = Math.abs(position1 - position2) / 25.0 * 4.0; //25 pikseli => 4km
        double time = DateUtils.calculateInMilliSeconds(timeStart, timeEnd) / 500.0;   //0.5s = 1h
        return (long) (distance / time);
    }

    /**
     * Getter dla maksymalnej prędkośći pojazdu
     * @return Maksymalna prędkość pojazdu
     */
    public int getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * Getter dla statystyk prędkości podczas różnych warunków pogodowych
     * @return Statystyki prędkości podczas różnych warunków pogodowych
     */
    public Map<WeatherEnum, List<Long>> getSpeedsForWeather() {
        return speedsForWeather;
    }

    /**
     * Getter dla aktualnie panującej pogody
     * @return Aktualnie ma
     */
    public WeatherEnum getCurrentWeather() {
        return currentWeather;
    }


    public boolean isInCollision() {
        return carIsInCollision;
    }

    public void markCollision() {
        carIsInCollision = true;
        collisionWeather = currentWeather;
    }

    public WeatherEnum getCollisionWeather() {
        return collisionWeather;
    }
}
