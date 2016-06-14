package main.model.belts;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.model.Car;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.DirectionEnum;
import main.model.enums.WeatherEnum;
import main.model.results.SpeedResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Krzysztof Baran
 * Klasa abstrakcyjna, reprezentująca dowolny pas drogowy, wymagający uszczegółowienia w klasach dziedziczących
 * Zajmuje się utzymaniem samochodów, ich poruszaniem/zwalnianiem, sprawdzaniem kolizji na pasach i skrzyżowaniach
 */
public abstract class TrafficBelt {
    /**
     * Wartość maksymalnych prób ruszenia każdym samochodem z osobna na pasie oraz dołożeniem nowego samochodu
     * Jeśli ilość prób przekroczy tą wartość, to zmieniane są światła (w algorytmie nr 2)
     */
    private static final Integer MAX_TRIES_FOR_LIGHTS_SWITCH_ALGORITHM = 200;
    /**
     * Współczynnyk odległości hamowania pojazdu
     */
    private static Double STOPPING_DIST_FACT = 1.5;
    /**
     * Wysokość pasa w pikselach
     */
    public static final int BELT_HEIGHT = 20;
    /**
     * Generator wartości losowych do generowania różych prędkości maksymalnych samochodów
     */
    private final Random random = new Random();
    /**
     * Kolejny numer pasa w zadanym kierunku
     */
    private final int beltNumber;
    /**
     * Limit samochodów na zadanym pasie
     */
    private int carsLimit;
    /**
     * Graficzna reprezentacja pasa drogowego
     */
    private Rectangle beltRect;
    /**
     * Lista samochodów, które zawierają się na zadanym pasie drogowym
     */
    protected List<Car> containingCars;
    /**
     * Kierunek pasa w jakim poruszają się samochody
     */
    private DirectionEnum beltDirection;
    /**
     * Współrzędne pasa (punkt startu i końca pasa)
     */
    protected int beltXStart, beltYStart, beltXEnd, beltYEnd;
    /**
     * Lista skrzyżowań występujących na danym pasie
     */
    protected List<TrafficLightsAndCrossing> crossingAndLights;
    /**
     * Wyniki prędkości wszystkich samochodów.
     * Zapisywane podczas usuwania samochodu ze sceny, który przejechał już cały dystans
     */
    protected List<SpeedResult> speedResults;
    /**
     * Mapa zawierająca listę samochodów, które opuściły dany pas w zadanych warunkach pogodowych
     */
    protected Map<WeatherEnum, Long> carsThatLeftTheStageWithWeather;
    /**
     * Liczba samochodów, które w ogóle opuściły pas po przejechaniu całego dystansu
     */
    protected long carsThatLeftTheStage;
    /**
     * Początek oraz koniec pomiaru odcinkowego, jeśli takowy istnieje
     */
    private final Integer speedControlXStart, speedControlXEnd;
    /**
     * Ilość nieudanych prób ruszenia samochodami, lub dołożenia samochodu na pas (gdy czekają na zielone światło).
     * Jeśli przekroczy limit, zmieniane są światła w algorytmie 2 i wartość jest zerowana.
     */
    private Integer addCarTriesFailure;

    /**
     * Konstruktor pasa
     * @param beltNumber Numer pasa
     * @param carsLimit Limit samochodów na zadanym pasie
     * @param xPos Wartość na osi X lewego górnego rogu pasa
     * @param yPos Wartość na osi Y lewego górnego rogu pasa
     * @param width Szerokość pasa (oś X)
     * @param height Wysokość pasa (oś Y)
     * @param beltDirection Kierunek poruszania się samochodów na pasie
     * @param speedControlXStart Początek pomiaru odcinkowego na osi X
     * @param speedControlXEnd Koniec pomiaru odcinkowego na osi X
     */
    public TrafficBelt(int beltNumber, int carsLimit, int xPos, int yPos, int width, int height, DirectionEnum beltDirection,
                       Integer speedControlXStart, Integer speedControlXEnd) {
        //Inicjalizacja podstawowych wartości
        carsThatLeftTheStageWithWeather = new HashMap<>();
        carsThatLeftTheStage = 0L;
        addCarTriesFailure = 0;
        this.beltNumber = beltNumber;
        this.carsLimit = carsLimit;
        speedResults = new ArrayList<>();
        this.containingCars = new ArrayList<>();
        //Utworzenie reprezentacji graficznej i nadanie kolorów, wysokości, szerokości
        beltRect = new Rectangle(xPos, yPos, width, height);
        beltRect.setFill(Color.web("0x34495E"));
        beltRect.setStroke(Color.web("0xC9CED4"));
        //Ustawienie kierunku pasa do przekazania go samochodom
        this.beltDirection = beltDirection;
        //Ustawienie pomiaru odcinkowego
        this.speedControlXStart = speedControlXStart;
        this.speedControlXEnd = speedControlXEnd;
    }

    /**
     * Abstrakcyjna metoda, która ma sprawdzić kolizje pomiędzy dwoma samochodami, zależnie od kierunku jazdy
     * @param car Samochód 1
     * @param c Samochód 2
     * @return True jeśli kolizja, wpp false
     */
    protected abstract boolean collisionBetweenTwoCars(Car car, Car c);

    /**
     * Abstrakcyjna metoda, która ma sprawdzić czy samochód ma jeszcze duży dystans do skrzyżowania
     * @param car Aktualnie analizowany samochód
     * @param nextCrossing Najbliższe skrzyżowanie do którego się zbliża
     * @return True jeśli ma spory dystans, wpp false
     */
    protected abstract boolean hasDistanceToCrossing(Car car, TrafficLightsAndCrossing nextCrossing) ;

    /**
     * Sprawdzenie, czy samochód ma zielone światło na następnym skrzyżowaniu (zależy od kierunku pasa)
     * @param nextCrossing Najbliższe skrzyżowanie do którego się zbliża
     * @return True jeśi światło jest zielone, wpp false
     */
    protected abstract boolean hasGreenLight(TrafficLightsAndCrossing nextCrossing) ;

    /**
     * Metoda znajdująca najbliższe skrzyżowanie, do którego zbliża się pojazd (zależy od kierunku pasa)
     * @param carPos Pozycja samochodu
     * @return Najbliższe skrzyżowanie
     */
    protected abstract TrafficLightsAndCrossing getNextCrossingAndLights(Point carPos);

    /**
     * Metoda, która znajduje wszystkie samochody, które opuściły scenę zwraca obiekty, woła logikę usunięcia samochodu,
     * funkcję cleanup oraz przekazuje listę samochodów do usunięcia z ekranu za pomocą jego kontrolera
     * @return Lista samochodów do usunięcia z ekranu
     */
    protected abstract List<ImageView> cleanup();

    /**
     * Getter inicjalizujący listę skrzyżowań jeśli jest nie zainicjalizowana i zwracający ową listę
     * @return Lista skrzyżowań z danym pasem
     */
    public List<TrafficLightsAndCrossing> getCrossingAndLights() {
        if (crossingAndLights == null) {
            crossingAndLights = new ArrayList<>();
        }
        return crossingAndLights;
    }

    /**
     * Metoda dodająca nowy samochód na pas.
     * Jeśli liczba samochodów przekracza limit, to samochód ten nie jest dodawany
     * @param weatherConditions
     * @return
     */
    public synchronized ImageView addCar(WeatherEnum weatherConditions) {
        //Jeśli liczba maksymalna pojazdów zostałaby przekroczona, to zwiększana jest ilość niepowodzeń, a pojazd
        //nie jest dodawany
        if (containingCars.size() >= carsLimit) {
            addCarTriesFailure++;
            return null;
        }
        //Utworzenie nowego pojazdy
        Car car = new Car(randomMaxSpeedForCar(weatherConditions), beltDirection, beltXStart, beltYStart, speedControlXStart, speedControlXEnd,
            weatherConditions);
        //Jeśli pojazd ma możliwe jakieś kolizje na pozycji początkowej (inny dodany samochód nie zdążył odjechać),
        //to samochód nie jest dodawany
        if (hasAnyPossibleCollision(car)) {
            return null;
        }
        //Dodanie nowego samochodu do pasa i zwrócenie go wyżej, aby można było dodać go do sceny w kontrolerze
        containingCars.add(car);
        return car;
    }

    /**
     * Metoda zmieniająca prędkości maksymalne samochodów jeśli zmianie uległa pogoda na scenie
     * @param weatherConditions Aktualna, nowa pogoda na scenie
     */
    public synchronized void changeCarsSpeed(WeatherEnum weatherConditions) {
        for (Car car : containingCars) {
            car.changeMaxSpeed(randomMaxSpeedForCar(weatherConditions), weatherConditions);
        }
    }

    /**
     * Ustawienie losowej prędkości do samochodu z uwzględnieniem maksymalnych i minimalnych wartości z warunków pogodowych
     * @param weatherConditions Aktualne warunki pogodowe
     * @return Obliczona prędkość samochodu
     */
    private synchronized int randomMaxSpeedForCar(WeatherEnum weatherConditions) {
        //Wybieramy czy samochód ma być szybsz, czy wolniejszy za pomocą współczynnika z warunków pogodowych
        if (random.nextInt(weatherConditions.getFasterCarProbabilityOneTo()) == 1) {
            //Inicjujemy prędkość szybszego samochodu z losową prędkością o maksimum z warunków pogodowych + minimum z tych warunków
            return random.nextInt(weatherConditions.getFasterRandomFactor()) + weatherConditions.getFasterMinFactor();
        }
        //Inicjujemy prędkość wolniejszego samochodu z losową prędkością o maksimum z warunków pogodowych + minimum z tych warunków
        return random.nextInt(weatherConditions.getSlowerRandomFactor()) + weatherConditions.getSlowerMinFactor();
    }

    /**
     * Getter dla graficznej reprezentacji pasa ruchu
     * @return Graficzna reprezentacja pasa ruchu
     */
    public Rectangle getBeltGraphics() {
        return beltRect;
    }

    /**
     * Metoda, której zadaniem jest sprawdzenie czy samochód może się poruszać dalej i jeśli tak, to wołana jest
     * funkcja go, wpp. samochód jest wyhamowywany funkcją stop
     * @return Lista obiektów graficznych, które po iteracji wyszły poza scenę i należy je usunąć w kontrolerze
     */
    public List<ImageView> moveCars() {
        for (Car car : containingCars) {
            //Jeśli samochód może jechać, to przyspieszamy go jeśli to możliwe i przesuwamy,
            //wpp hamujemy go i przesuwamy, ew. zatrzymujemy jeśli prędkość spadła do 0
            if (canCarGo(car)) {
                car.go();
            } else {
                car.stop();
            }
            //Sprawdzamy nowe pozycje samochodów i jeśli pokrywają się częściowo z jakimś skrzyżowaniem,
            //to dodajemy ten samochód do skrzyżowania w celu późniejszej detekcji kolizji.
            //Jeśli natomiast samochód znajduje się już liście samochodów na skrzyżowaniu, a wyjechał poza nie,
            //to usuwamy go z listy
            for (TrafficLightsAndCrossing crossing : crossingAndLights) {
                if (crossing.isCarOnTheCrossing(car.getPosition(), BELT_HEIGHT)) {
                    crossing.addCarIfNotExists(car);
                } else if (!crossing.isCarOnTheCrossing(car.getPosition(), BELT_HEIGHT)) {
                    crossing.removeCar(car);
                }
            }
        }
        //Oznaczamy wszystkie kolizje jakie wystąpiły po zadanej iteracji
        markCollisionCars();
        //zwracamy listę samochodów do usunięcia ze sceny
        return cleanup();
    }

    /**
     * Metoda sprawdzająca, czy zadany samochód może jechać.
     * Sprawdzenia są następujące:
     * 1. Czy samochód ma jakieś prawdopodobne kolizje na pasie
     * 2. Czy samochód ma zielone światło i nie powoduje kolizji na połowie pasów skrzyżowania,
     *      aby swobodnie przez nie przejechać
     * 3. Czy ma czerwone światło i ma nadal dystans do tego skrzyżowania, który może pokonać
     * @param car Aktualnie analizowany samochód
     * @return True jeśli samochód może jechać, wpp false
     */
    private boolean canCarGo(Car car) {
        Point carPos = car.getPosition();
        //Znajdujemy najbliższe skrzyżowanie
        TrafficLightsAndCrossing nextCrossing = getNextCrossingAndLights(carPos);
        //Sprawdzamy, czy samochód ma jakiekolwiek prawdopodobne kolizje na pasie na którym się znajduje
        if (hasAnyPossibleCollision(car)) {
            return false;
        }
        //Sprawdzamy, czy samochód ma zielone światło i może swobodnie przejechać skrzyżowanie, aby z niego zjechać
        if (hasGreenLight(nextCrossing) && !hasCollisionOnTheCurrentCrossing(nextCrossing, car)) {
            return true;
        }
        //Jeśli samochód ma czerwone światło, to zwiększamy ilość porażek, aby szybciej je zmienić dla alg.2
        if (!hasGreenLight(nextCrossing)) {
            addCarTriesFailure++;
        }
        //Sprawdzenie, czy ma czerwone światło, ale nadal ma dużą odległość do skrzyżowania
        if (nextCrossing == null || hasDistanceToCrossing(car, nextCrossing)) {
            return true;
        }
        return false;
    }

    /**
     * Metoda sprawdzająca, czy po przekroczeniu połowy pasów skrzyżowania, samochód nie spowoduje kolizji z innym
     * @param crossing Aktualne skrzyżowanie
     * @param car Aktualnie analizowany samochód
     * @return True gdy spowoduje kolizję w trzech ruchach, wpp false.
     */
    private boolean hasCollisionOnTheCurrentCrossing(TrafficLightsAndCrossing crossing, Car car) {
        if (crossing == null) {
            return false;
        }
        //Jeśli ma duży dystans do skrzyżowania, to nie ma sensu sprawdzać kolizji
        if (hasDistanceToCrossing(car, crossing)) {
            return false;
        }
        Car tempCar = new Car(car);
        //Iterujemy połowę razy liczbę pasów pionowych na skrzyżowaniu
        //Za każdym razem ruszamy tymczasowym samochodem i sprawdzamy, czy nie zachodzi kolizja z żadnym samochodem,
        //który znajduje się na skrzyżowaniu
        for (int i = 0; i <= crossing.getVBeltsCount() + 1; i++) {
            tempCar.go();
            for (Car c : crossing.getContainingCars()) {
                if (c == car) {
                    continue;
                }
                //Sprawdzenie pełnej kolizji
                if (checkFullCollisionBetweenTwoCars(c, tempCar)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void markCollisionCars() {
        for (Car car1 : containingCars) {
            for (Car car2 : containingCars) {
                if (car1 == car2) {
                    continue;
                }
                if (checkFullCollisionBetweenTwoCars(car1, car2)) {
                    if (!car1.isInCollision()) {
                        car1.markCollision();
                    }
                    if (!car2.isInCollision()) {
                        car2.markCollision();
                    }
                }
            }
        }
    }

    /**
     * Sprawdzenie kolizji dwóch samochodów używając obu osi X, Y
     * @param c Samochód 1
     * @param tempCar Samochód 2
     * @return True jeśli kolizja, false wpp
     */
    protected boolean checkFullCollisionBetweenTwoCars(Car c, Car tempCar) {
        boolean yCollision = ((c.getY() >= tempCar.getY() && c.getY() <= tempCar.getY() + BELT_HEIGHT) ||
                (tempCar.getY() >= c.getY() && tempCar.getY() <= c.getY() + BELT_HEIGHT));
        boolean xCollision = ((c.getX() >= tempCar.getX() && c.getX() <= tempCar.getX() + BELT_HEIGHT) ||
                (tempCar.getX() >= c.getX() && tempCar.getX() <= c.getX() + BELT_HEIGHT));
        return xCollision && yCollision;
    }

    /**
     * Sprawdzenie, czy samochód ma potencjalne kolizje na tym samym pasie na którym się znajduje
     * @param car Aktualnie analizowany samochód
     * @return True jeśli kolizja, wpp false
     */
    private boolean hasAnyPossibleCollision(Car car) {
        for (Car c : containingCars) {
            if (c == car) {
                continue;
            }
            //Sprawdzenie kolizji dla każdego z pasów
            if (collisionBetweenTwoCars(car, c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metoda usuwająca samochody, które opuściły już pas.
     * Następnie zbiera dane statystyczne, dodaje ilość samochodów dla aktualnej pogody (gdyż w niej opuściły scenę)
     * oraz aktualizuje ilość samochodów, które opuściły scenę
     * @param carsToRemove
     */
    protected void clear(List<Car> carsToRemove) {
        synchronized (containingCars) {
            containingCars.removeAll(carsToRemove);
            speedResults.addAll(carsToRemove.stream().map(car -> new SpeedResult(car.getAverageSpeed(), car.getRadarMeasuredSpeed(), car.getSpeedsForWeather()))
                .collect(Collectors.toList()));
            countWeatherCars(carsToRemove);
            carsThatLeftTheStage += carsToRemove.size();
        }
    }

    /**
     * Metoda, która dodaje do mapy ilość samochodów, które opuściły scenę w aktualnej pogodzie
     * @param carsToRemove Lista samochodów, które zostaną usunięte
     */
    private synchronized void countWeatherCars(List<Car> carsToRemove) {
        //Dla każdego samochodu
        for (Car car : carsToRemove) {
            //Jeśli mapa nie ma w kluczach danej pogody, to ją inicjalizujemy z zerem pojazdów
            if (!carsThatLeftTheStageWithWeather.containsKey(car.getCurrentWeather())) {
                carsThatLeftTheStageWithWeather.put(car.getCurrentWeather(), 0L);
            }
            //Dodajemy 1 dla pojazdu i pogody jaka dla niego panowała
            carsThatLeftTheStageWithWeather.put(car.getCurrentWeather(), carsThatLeftTheStageWithWeather.get(car.getCurrentWeather()) + 1);
        }
    }

    /**
     * Getter dla kierunku poruszania się po pasie drogowym
     * @return Kierunek poruszania się po pasie drogowym
     */
    public DirectionEnum getBeltDirection() {
        return beltDirection;
    }

    /**
     * Getter dla statystyk prędkościowych i pogodowych każdego z samochodów
     * @return Statystyki prędkościowe i pogodowe każdego z samochodów
     */
    public List<SpeedResult> getSpeedResults() {
        return speedResults;
    }

    /**
     * Getter dla współczynnika odległości hamowania w zależności od pogody
     * @return Współczynnik odległości hamowania w zależności od pogody
     */
    public static double getStoppingDistFact() {
        synchronized (STOPPING_DIST_FACT) {
            return STOPPING_DIST_FACT;
        }
    }

    /**
     * Setter dla współczynnika odległości hamowania w zależności od pogody
     * @param stoppingDistFact Współczynnik odległości hamowania w zależności od pogody
     */
    public static void setStoppingDistFact(double stoppingDistFact) {
        synchronized (STOPPING_DIST_FACT) {
            STOPPING_DIST_FACT = stoppingDistFact;
        }
    }

    /**
     * Getter dla numeru pasa
     * @return Numer pasa
     */
    public int getBeltNumber() {
        return beltNumber;
    }

    /**
     * Sprawdzenie, czy ilość prób dodania samochodu/ruszenia samochodu przekroczyła limit
     * @return True, jeśli ilość prób dodania samochodu/ruszenia samochodu przekroczyła limit, wpp false
     */
    public synchronized boolean isAboveMaxTries() {
        return this.addCarTriesFailure > MAX_TRIES_FOR_LIGHTS_SWITCH_ALGORITHM;
    }

    /**
     * Metoda resetująca zliczanie ilości prób dodania samochodu/ruszenia samochodu
     */
    public synchronized void resetCarsTriesCounter() {
        this.addCarTriesFailure = 0;
    }

    /**
     * Getter dla mapy samochodów, które opuściły scenę w zadanych warunkach pogodowych
     * @return Mapa samochodów, które opuściły scenę w zadanych warunkach pogodowych
     */
    public Map<WeatherEnum, Long> getCarsThatLeftTheStageWithWeather() {
        return carsThatLeftTheStageWithWeather;
    }

    /**
     * Getter dla ilości samochodów, które w ogóle opuściły scenę
     * @return Ilość samochodów, które w ogóle opuściły scenę
     */
    public long getCarsThatLeftTheStage() {
        return carsThatLeftTheStage;
    }
}
