package main.controllers;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import main.model.belts.DownTrafficBelt;
import main.model.belts.LeftTrafficBelt;
import main.model.belts.RightTrafficBelt;
import main.model.SpeedRadar;
import main.model.belts.TrafficBelt;
import main.model.TrafficLightsAndCrossing;
import main.model.belts.UpTrafficBelt;
import main.model.enums.AlgorithmType;
import main.model.enums.WeatherEnum;
import main.utils.DateUtils;
import main.utils.ExcelUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Krzysztof Baran
 * Kontroler odpowiedzialny za obsługę warstwy logicznej symulacji oraz wywołanie zmian na ekranie przez terrain controllera
 */
public class SimulationController {
    /**
     * Wybrany przez użytkownika typ algorytmu
     */
    private final AlgorithmType algorithmType;
    /**
     * Lista przechowująca pasy pionowe na pierwszym skrzyżowaniu
     */
    private List<TrafficBelt> verticalBelts;
    /**
     * Lista przechowująca pasy pionowe na drugim skrzyżowaniu
     */
    private List<TrafficBelt> verticalBelts2;
    /**
     * Lista przechowująca pasy poziome
     */
    private List<TrafficBelt> horizontalBelts;
    /**
     * Lista przechowująca skrzyżowania i światła na nich zawarte
     */
    private List<TrafficLightsAndCrossing> crossings;
    /**
     * Lista posiadająca obiekty graficzne reprezentujące początek i koniec pomiaru odcinkowego
     */
    private List<SpeedRadar> speedRadars;
    /**
     * Zmienna trzymająca czas symulacji wybrany przez użytkownika
     */
    private int simulationTime;
    /**
     * Aktualnie panujące wartości pogodowe
     */
    private WeatherEnum weatherConditions;
    /**
     * Lista przechowująca następujące po sobie warunki pogodowe na scenie
     */
    private List<WeatherEnum> weatherConditionsList;
    /**
     * Flaga mówiąca o tym, czy pogoda ma zmieniać się dynamicznie
     */
    private final boolean dynamicWeather;
    /**
     * Flaga mówiąca o tym, czy pogoda ma być iterowana po kolei w stałym czasie (true), czy ma być czysto losowa (false) jeśli ustawiona randomowa
     */
    private final boolean allWeatherIteration;

    /**
     * Konstruktor kotrolera symulacji, inicjalizujący odpowiednie wartości przed startem
     * @param weatherConditions Warunki pogodowe
     * @param algorithmType Typ algorytmu zmiany świateł
     * @param verticalBeltsCount Liczba pasów pionowych na skrzyżowaniu pierwszym
     * @param verticalBelts2Count Liczba pasów pionowych na skrzyżowaniu drugim
     * @param horizontalBeltsCount Liczba pasów poziomych
     * @param carsLimit Limit samochodów na każdym z pasów
     * @param simulationTime Czas trwania symulacji, ustawiony przez użytkownika
     * @param width Szerokość okna
     * @param height Wysokość okna
     */
    public SimulationController(WeatherEnum weatherConditions, AlgorithmType algorithmType, int verticalBeltsCount, int verticalBelts2Count,
                                int horizontalBeltsCount,
                                int carsLimit, int simulationTime, int width, int height) {
        //Jeśli warunki pogodowe przekazane z okna są puste, świadczy to o dynamicznie zmiennej pogodzie
        this.dynamicWeather = weatherConditions == null || weatherConditions == WeatherEnum.ALL;
        this.allWeatherIteration = weatherConditions == WeatherEnum.ALL;
        //Jeśli pogoda jest dynamiczna, to losujemy, wpp przypisujemy wybraną lub kolejną jeśli ustawione na pogody po kolei
        this.weatherConditions = weatherConditions == null ? WeatherEnum.getRandom() :
            (weatherConditions == WeatherEnum.ALL ? WeatherEnum.getNext(weatherConditions) : weatherConditions);
        //Zapisujemy historyczą wartość ostatniej pogody panującej na scenie
        weatherConditionsList = new ArrayList<>();
        weatherConditionsList.add(this.weatherConditions);
        //Inicjalizacja pasów pionowych na skrzyżowaniu pierwszym
        verticalBelts = initVerticalBelts(verticalBeltsCount, height, width, carsLimit, width / 4);
        //Inicjalizacja pasów pionowych na skrzyżowaniu drugim
        verticalBelts2 = initVerticalBelts(verticalBelts2Count, height, width, carsLimit, 3 * width / 4);
        //Inicjalizacja pasów poziomych
        horizontalBelts = initHorizontalBelts(horizontalBeltsCount, height, width, carsLimit, verticalBeltsCount, verticalBelts2Count);
        //Inicjalizacja skrzyżowań pasów
        crossings =
            initCrossings(verticalBeltsCount, verticalBelts2Count, horizontalBeltsCount, height, width);
        //Ustawiamy współczynnik odległości hamowania i odpowiednich skrzyżowań na każdym z pasów
        for (TrafficBelt belt : horizontalBelts) {
            belt.setStoppingDistFact(this.weatherConditions.getStoppingDistanceFactor());
            belt.getCrossingAndLights().add(crossings.get(0));
            belt.getCrossingAndLights().add(crossings.get(1));
        }
        for (TrafficBelt belt : verticalBelts) {
            belt.setStoppingDistFact(this.weatherConditions.getStoppingDistanceFactor());
            belt.getCrossingAndLights().add(crossings.get(0));
        }
        for (TrafficBelt belt : verticalBelts2) {
            belt.setStoppingDistFact(this.weatherConditions.getStoppingDistanceFactor());
            belt.getCrossingAndLights().add(crossings.get(1));
        }
        //Przypisujemy czas symulacji i typ algorytmu
        this.simulationTime = simulationTime;
        this.algorithmType = algorithmType;

    }

    /**
     * Metoda odpowiadająca za wystartowanie symulacji
     * @param terrainController Kontroler graficznej reprezentacji ekranu symulacji
     */
    public void startSimulation(final TerrainController terrainController) {
        //Ustawiamy 30 klatek na sekundę, aby animacja była płynna
        final int taskPeriod = 30;
        //Obliczamy ile iteracji potrzebujemy, aby zachować czas symulacji przy ustalonej ilości klatek na sekundę
        final double[] interval = { (double) simulationTime * 1000.0 / (double) taskPeriod };
        //Inicjalizujemy timer
        Timer timer = new Timer(true);
        //Ustawiamy czas startu symulacji, aby później było możliwe jego obliczenie
        final LocalDateTime iterationStart = LocalDateTime.now();
        //Ustawiamy iteracje ze stały czasem wywołania (co 1/30 sekundy), z sekundowym (1000ms) opóźnieniem startu
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Wywołujey iterację symulacji
                simulationIteration(terrainController);
                //Sprawdzamy, czy jeszcze powinniśmy wywoływać kolejną iterację?
                synchronized (interval) {
                    if (--interval[0] <= 1) {
                        //Jeśli nie kontynuujemy, to zamykamy timer
                        timer.cancel();
                        synchronized (weatherConditionsList) {
                            //Po zamknięciu timera, zbieramy rezultaty symulacji i zapisujemy je na dysku
                            String resFilename = collectResultsToFiles(DateUtils.calculateInSeconds(iterationStart, LocalDateTime.now()),
                                weatherConditionsList);
                            //W wątku UI inicjalizujemy i pokazujemy ekran podsumowania
                            Platform.runLater(() -> terrainController.showResultsScreen(resFilename));
                        }
                    }
                }
            }
        }, 1000, taskPeriod);
        //Jeśli wybranym algorytmem zmiany świateł jest stały interwał czasowy, to startujemy timer2,
        //który jest odpowiedzialny za ich zimanę w stałym interwale czasowym
        if (algorithmType == AlgorithmType.FIXED_TIME) {
            Timer timer2 = new Timer(true);
            //Ustawiamy 3sek (3000ms) opóźnienia pierwszej zmiany świateł i zmieniamy je co 6sek (6000ms)
            timer2.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    //Jeśli nie kontynuujemy symulacji, gdyż czas się skończył, zamykamy timer
                    synchronized (interval) {
                        if (interval[0] <= 1) {
                            timer2.cancel();
                            return;
                        }
                    }
                    //Dla każdego ze skrzyżowań, zmieniamy światła
                    for (TrafficLightsAndCrossing cross : crossings) {
                        cross.changeLights();
                    }
                }
            }, 3000, 6000);
        }
        //Jeśli ustawiliśmy dynamiczną pogodę, inicjalizujemy kolejny timer, który będzie ją zmieniał w stałym czasie
        //Pogoda zmieni się z pierwszym opóźnieniem 6sek (6000ms) i będzie się zmieniać co 18sek (18000ms)
        //Natomiast jeśli pogoda ma się zmieniać po kolei, to:
        //obliczamy czas zmiany tak, aby wszystkie pogody panowały w równym przedziale czasowym
        //opóźnienie pierwszej zmiany ustawiamy na przedział czasowy + 1sek, aby zrównać opóźnienie startu symulacji
        if (dynamicWeather) {
            Timer weatherTimer = new Timer(true);
            final int weatherChangePeriod = allWeatherIteration ? (simulationTime * 1000 / WeatherEnum.getAllowedWeatherCount()) : 18000;
            final int weatherFirstChangeDelay = allWeatherIteration ? weatherChangePeriod + 1000 : 6000;
            weatherTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    synchronized (weatherConditions) {
                        //Pobieramy nową losową pogodę z enumeratora warunków pogodowych
                        weatherConditions = allWeatherIteration ? WeatherEnum.getNext(weatherConditions) : WeatherEnum.getRandom();
                        //Zapisujemy nową pogodę do listy z historią pogody
                        synchronized (weatherConditionsList) {
                            weatherConditionsList.add(weatherConditions);
                        }
                        //Zmieniamy znak poogdy w wątku UI
                        Platform.runLater(() -> terrainController.setWeatherSign(weatherConditions));
                        //Ustawiamy współczynnik odległości hamowania z wylosowanych warunków pogodowych
                        TrafficBelt.setStoppingDistFact(weatherConditions.getStoppingDistanceFactor());
                        //Dla każdego pasa zmieniamy prędkość obliczamy prędkość maksymalną pojazdów zgodnie
                        //z parametrami w nowych warunkach pogodowych
                        for (TrafficBelt belt : getAllBelts()) {
                            belt.changeCarsSpeed(weatherConditions);
                        }
                    }
                }
            }, weatherFirstChangeDelay, weatherChangePeriod);
        }
    }

    /**
     * Metoda pomocnicza, zbierająca informację do excela i zapisująca je na dysku
     * @param simulationTimeInSeconds Czas trwania symulacji w sekundach
     * @param weatherConditionsList Lista warunków pogodowych jakie trwały w czasie symulacji
     * @return Nazwa pliku zapisanego na dysku
     */
    private synchronized String collectResultsToFiles(long simulationTimeInSeconds, List<WeatherEnum> weatherConditionsList) {
        return ExcelUtils.exportResults(getAllBelts(), weatherConditionsList, simulationTimeInSeconds);
    }

    /**
     * Metoda wywołująca wszystkie składowe jednej iteracji symulacji
     * @param terrainController Kontroler graficznej reprezentacji ekranu symulacji
     */
    private void simulationIteration(final TerrainController terrainController) {
        //W wątku UI:
        Platform.runLater(() -> {
            //Flaga, mówiąca o tym, czy powinniśmy zmienić światła, jeśli wybrany został algorytm ziany świateł
            //w zależności od ilości samochodów
            boolean changeLightsSwitch = false;
            //Dla każdego z pasów
            for (TrafficBelt belt : SimulationController.this.getAllBelts()) {
                //Przesuwamy wszystkie samochody w iteracji i usuwamy z ekranu za pomocą terrainControllera
                //samochody, które opuściły już scenę
                terrainController.removeCarsFromStage(belt.moveCars());
                //Próbujemy dodać nowy samochód dla parametrów z obecnych warunków pogodowych
                ImageView res = belt.addCar(weatherConditions);
                //Jeśli się udało dodać go na pas, to dodajemy do ekranu za pomocą terrainControllera
                if (res != null) {
                    terrainController.addCarOnStage(res);
                }
                //Jeśli przekroczyliśmy dla któregoś z pasów maksymalną ilość niepowodzeń ruchu samochodów lub
                //ich dodania do pasa, a wybrany jest algorytm zmiany świateł w zleżności od tych prób,
                //zaznaczamy flagę zmiany świateł
                if (belt.isAboveMaxTries() && algorithmType == AlgorithmType.CARS_COUNT) {
                    changeLightsSwitch = true;
                }
            }
            //Jeśli wybrany został algorytm zmiany świateł względem samochodów i powinniśmy je zmienić,
            //to dla każdego ze skrzyżowań robimy ich zmianę
            if (changeLightsSwitch) {
                SimulationController.this.getAllBelts().forEach(TrafficBelt::resetCarsTriesCounter);
                crossings.forEach(TrafficLightsAndCrossing::changeLights);
            }
        });
    }

    /**
     * Metoda inicjalizująca skrzyżowania drogowe.
     * Przeliczana jest ich szerokość i wysokość w zależności od ilości pasów w każdej z osi
     * i zwracana jest lista utworzonych skrzyżowań
     * @param verticalBeltsCount Liczba pasów pionowych na skrzyżowaniu pierwszym
     * @param verticalBelts2Count Liczba pasów pionowych na skrzyżowaniu drugim
     * @param horizontalBeltsCount Liczba pasów poziomych
     * @param windowHeight Wysokość okna
     * @param windowWidth Szerokość okna
     * @return Lista zainicjalizowanych skrzyżowań
     */
    private List<TrafficLightsAndCrossing> initCrossings(int verticalBeltsCount, int verticalBelts2Count,
                                                         int horizontalBeltsCount, int windowHeight, int windowWidth) {
        List<TrafficLightsAndCrossing> res = new ArrayList<>();
        //Przeliczamy początek i koniec obu skrzyżowań na osi oY
        int yFrom = windowHeight / 2 - horizontalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;
        int yTo = windowHeight / 2 + horizontalBeltsCount * TrafficBelt.BELT_HEIGHT + 3;
        //Przeliczamy początek i koniec pierwszego skrzyżowaia na osi oX
        int x1From = windowWidth / 4 - verticalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;
        int x1To = windowWidth / 4 + verticalBeltsCount * TrafficBelt.BELT_HEIGHT + 3;
        //Przeliczamy początek i koniec drugiego skrzyżowaia na osi oX
        int x2From = 3 * windowWidth / 4 - verticalBelts2Count * TrafficBelt.BELT_HEIGHT - 3;
        int x2To = 3 * windowWidth / 4 + verticalBelts2Count * TrafficBelt.BELT_HEIGHT + 3;
        //Dodajemy skrzyżowania do listy wynikowej
        res.add(new TrafficLightsAndCrossing(x1From, x1To, yFrom, yTo, verticalBeltsCount));
        res.add(new TrafficLightsAndCrossing(x2From, x2To, yFrom, yTo, verticalBelts2Count));
        return res;
    }

    /**
     * Metoda inicjalizująca pasy pionowe na skrzyżowaniu.
     * Przeliczane są ich wysokości oraz pozycje
     * @param verticalBeltsCount Liczba pasów pionowych na skrzyżowaniu pierwszym
     * @param windowHeight Wysokość okna
     * @param windowWidth Szerokość okna
     * @param carsLimit Limit samochodów na każdym z pasów
     * @param offset Przesunięcie w zależności, czy jest to skrzyżowanie pierwsze, czy drugie. Jest to środek pasów
     * @return Lista zainicjowanych pasów
     */
    private List<TrafficBelt> initVerticalBelts(int verticalBeltsCount, int windowHeight, int windowWidth, int carsLimit, int offset) {
        List<TrafficBelt> res = new ArrayList<>();
        //Przeliczamy początek pasów na osi oX i uwzględniamy rozdzielenie oby kierunków o 6px
        int beltsXStart = offset - verticalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;
        //Kolejny numer pasa
        int beltNr = 1;
        //Tworzymy pasy o kierunku w dół i wysokości = wysokość okna + 20px (aby samochód mógł się schować poza ekran)
        for (int i = 0; i < verticalBeltsCount; i++) {
            TrafficBelt belt =
                new DownTrafficBelt(beltNr++, carsLimit, beltsXStart, 0, TrafficBelt.BELT_HEIGHT, windowHeight + 20, null, null);
            res.add(belt);
            beltsXStart += TrafficBelt.BELT_HEIGHT;
        }
        //Rozdzielamy oba kierunki o 6px
        beltsXStart += 6;
        //Kolejny numer pasa liczymy od początku, bo to już inny kierunek
        beltNr = 1;
        //Tworzymy pasy o kierunku w górę i wysokości = wysokość okna + 20px (aby samochód mógł się schować poza ekran)
        for (int i = 0; i < verticalBeltsCount; i++) {
            TrafficBelt belt =
                new UpTrafficBelt(beltNr++, carsLimit, beltsXStart, 0, TrafficBelt.BELT_HEIGHT, windowHeight + 20, null, null);
            res.add(belt);
            beltsXStart += TrafficBelt.BELT_HEIGHT;
        }
        return res;
    }

    /**
     * Metoda inicjalizująca pasy pionowe na skrzyżowaniu.
     * Przeliczane są ich wysokości oraz pozycje.
     * Dodawane są również punkty pomiarów odcinkowych (start i koniec)
     * @param horizontalBeltsCount Liczba pasów poziomych
     * @param windowHeight Wysokość okna
     * @param windowWidth Szerokość okna
     * @param carsLimit Limit samochodów na każdym z pasów
     * @param verticalBeltsCount Liczba pasów pionowych na skrzyżowaniu pierwszym
     * @param verticalBelts2Count Liczba pasów pionowych na skrzyżowaniu drugim
     * @return Lista zainicjalizowanych pasów poziomych
     */
    public List<TrafficBelt> initHorizontalBelts(int horizontalBeltsCount, int windowHeight, int windowWidth, int carsLimit,
                                                 int verticalBeltsCount, int verticalBelts2Count) {
        List<TrafficBelt> res = new ArrayList<>();
        speedRadars = new ArrayList<>();
        //Obliczamy początek i koniec pomiaru odcinkowego dla pasa o kierunku poruszania w lewo
        int leftBeltSpeedControlStart = (int)(3.0 * windowWidth / 4.0 - verticalBeltsCount * (TrafficBelt.BELT_HEIGHT + 3.0)) - 10;
        int leftBeltSpeedControlEnd = (int)(leftBeltSpeedControlStart - windowWidth / 6.0);
        //Obliczamy początek i koniec pomiaru odcinkowego dla pasa o kierunku poruszania w prawo
        int rightBeltSpeedControlStart = (int) (windowWidth / 4.0 + verticalBelts2Count * (TrafficBelt.BELT_HEIGHT + 3.0)) + 10;
        int rightBeltSpeedControlEnd = (int) (rightBeltSpeedControlStart + windowWidth / 6.0);
        //Obliczamy punkt startu pierwszego pasa na osi oY z uwzględnieniem rozdzielenia obu kierynków o 6px
        int beltsYStart = windowHeight / 2 - horizontalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;
        //Dodajemy reprezentację graficzną pomiaru odcinkowego na pasie w lewo
        speedRadars.addAll(Arrays.asList(new SpeedRadar(leftBeltSpeedControlStart - 10, beltsYStart - 40)));
        speedRadars.addAll(Arrays.asList(new SpeedRadar(leftBeltSpeedControlEnd - 10, beltsYStart - 40)));
        //Kolejny numer pasa
        int beltNr = 1;
        //Tworzymy pasy o kierunku w lewo i szerokości = szerokość okna + 20px (aby samochód mógł się schować poza ekran)
        for (int i = 0; i < horizontalBeltsCount; i++) {
            TrafficBelt belt = new LeftTrafficBelt(beltNr++, carsLimit, 0, beltsYStart, windowWidth + 20, TrafficBelt.BELT_HEIGHT,
                leftBeltSpeedControlStart, leftBeltSpeedControlEnd);
            res.add(belt);
            beltsYStart += TrafficBelt.BELT_HEIGHT;
        }
        //Ustawiamy odstęp między kierunkami jazdy na 6px
        beltsYStart += 6;
        //Koleny numer pasa w prawym kierunku
        beltNr = 1;
        //Tworzymy pasy o kierunku w prawo i szerokości = szerokość okna + 20px (aby samochód mógł się schować poza ekran)
        for (int i = 0; i < horizontalBeltsCount; i++) {
            TrafficBelt belt = new RightTrafficBelt(beltNr++, carsLimit, 0, beltsYStart, windowWidth + 20, TrafficBelt.BELT_HEIGHT,
                rightBeltSpeedControlStart, rightBeltSpeedControlEnd);
            res.add(belt);
            beltsYStart += TrafficBelt.BELT_HEIGHT;
        }
        //Dodajemy reprezentację graficzną pomiaru odcinkowego na pasie w prawo
        speedRadars.addAll(Arrays.asList(new SpeedRadar(rightBeltSpeedControlStart + 10, beltsYStart + 10)));
        speedRadars.addAll(Arrays.asList(new SpeedRadar(rightBeltSpeedControlEnd + 10, beltsYStart + 10)));
        return res;
    }

    /**
     * Metoda dodaje wszystkie pasy na jedną listę i zwraca ją
     * @return Lista wszystkich pasów drogowych
     */
    public List<TrafficBelt> getAllBelts() {
        List<TrafficBelt> res = new ArrayList<>();
        res.addAll(horizontalBelts);
        res.addAll(verticalBelts);
        res.addAll(verticalBelts2);
        return res;
    }

    /**
     * Getter dla wszystkich skrzyżowań
     * @return Wszystkie skrzyżowania
     */
    public List<TrafficLightsAndCrossing> getCrossings() {
        return crossings;
    }

    /**
     * Getter dla aktualnych warunków pogodowych
     * @return Aktualne warunki pogodowe
     */
    public WeatherEnum getWeatherConditions() {
        return weatherConditions;
    }

    /**
     * Getter dla reprezentacji graficznej obiektów na początku i końcu pomiaru odcinkowego
     * @return Reprezentacja graficzna obiektów na początku i końcu pomiaru odcinkowego
     */
    public List<SpeedRadar> getSpeedRadars() {
        return speedRadars;
    }
}
