package main.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import main.model.SpeedLimitSign;
import main.model.belts.TrafficBelt;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.AlgorithmType;
import main.model.enums.WeatherEnum;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Krzysztof Baran
 * Klasa kontrolera ekranu symulacji.
 * Jej zadaniem jest obsłużenie elementów graficznych ekranu, wystartowanie symulacji oraz utworzenie ekranu podsumowania
 */
public class TerrainController implements Initializable {

    /**
     * Oryginalny rozmiar obrazu informującego o pogodzie
     */
    private static double weatherOriginalImageWidth = 801.0;
    private static final double weatherOriginalImageHeight = 323.0;
    /**
     * Skala w jakiej musimy pomniejszyć obrazek informujący o pogodzie
     */
    private static final double weatherImageScale = 6.0;
    /**
     * Panel główny symulacji, na którym prezentowana jest cała symulacja
     */
    @FXML
    Pane terrainMainPanel;
    /**
     * Przycisk startu symulacji
     */
    @FXML
    Button startSimulationButton;
    /**
     * Szerokość okna z podsumowaniem
     */
    private static int WINDOW_WIDTH = 800;
    /**
     * Wysokość okna z podsumowaniem
     */
    private static int WINDOW_HEIGHT = 600;
    /**
     * Kontroler odpowiedzialny za obsługę warstwy logicznej symulacji
     */
    private SimulationController simulationController;
    /**
     * Obraz prezentujący aktualną pogodę na scenie
     */
    private ImageView currentWeather;
    /**
     * Szerokość okna symulacji
     */
    private double windowWidth;

    /**
     * Metoda wołana podczas tworzenia TerrainControllera przez framework JavaFX
     * @param location Lokacja względem której możemy ustalać względne ścieżki (ignorowana na tym etapie)
     * @param resources Obiekt za pomocą którego możemy pobierać resource aplikacji (ignorowany na tym etapie)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Obsługa przycisku startu symulacji
        startSimulationButton.setOnAction(event -> {
            //Jeśli kontroler symulacji nie jest zainicjalizowany, to nic nie robimy
            if (simulationController == null) {
                return;
            }
            //W oddzielnym tasku definiujemy rozpoczęcie symulacji, tak aby nie blokować wątku wyświetlającego,
            //a następnie startujemy task
            new Thread(new Task() {
                @Override
                protected Object call() throws Exception {
                    //Startujemy symulację
                    simulationController.startSimulation(TerrainController.this);
                    //Wyłączamy przycisk startu symulacji w wątku UI
                    Platform.runLater(() -> startSimulationButton.setDisable(true));
                    return null;
                }
            }).start();
        });
    }

    /**
     * Metoda dodająca obiekt samochodu do sceny, aby był widoczny na ekranie
     * @param car Obraz z samochodem do dodania na scenie
     */
    public void addCarOnStage(ImageView car) {
        terrainMainPanel.getChildren().add(car);
    }

    /**
     * Metoda usuwająca obiekt samochodu ze sceny, aby nie przeładowywać pamięci
     * @param cars Obraz z samochodem do usunięcia ze sceny
     */
    public void removeCarsFromStage(List<ImageView> cars) {
        if (cars != null && !cars.isEmpty()) {
            terrainMainPanel.getChildren().removeAll(cars);
        }
    }

    /**
     * Metoda inicjalizująca wszystkie potrzebne parametry kontrolera, pobierane z ekranu głównego
     * @param weatherConditions Warunki pogodowe wybrane przez użytkownika
     * @param algorithmType Typ algorytmu wybrany przez użytkownika
     * @param verticalBeltsCount Ilość pasów pionowych na pierwszym skrzyżowaniu
     * @param verticalBelts2Count Ilość pasów pionowych na drugim skrzyżowaniu
     * @param horizontalBeltsCount Ilość pasów poziomych
     * @param carsLimit Limit samochodów na każdym z pasów
     * @param simulationTime Czas trwania symulacji wybrany przez użytkownika
     * @param width Szerokość okna
     * @param height Wysokość okna
     */
    public void initControllerValues(WeatherEnum weatherConditions, AlgorithmType algorithmType, int verticalBeltsCount,
                                     int verticalBelts2Count, int horizontalBeltsCount,
                                     int carsLimit, int simulationTime, int width, int height) {
        this.windowWidth = width;
        //Utworzenie kontrolera symulacji potrzebnymi wartościami
        simulationController = new SimulationController(weatherConditions, algorithmType, verticalBeltsCount, verticalBelts2Count, horizontalBeltsCount,
            carsLimit, simulationTime, width, height);
        //Dla każdego z pasów, które zostały zainicjowane w kontrolerze symulacji, dodawane są ich
        //reprezentacje na ekranie
        for (TrafficBelt belt : simulationController.getAllBelts()) {
            terrainMainPanel.getChildren().add(belt.getBeltGraphics());
        }
        //Dla każdego ze skrzyżowań, które zostały zainicjowane w kontrolerze symulacji, dodawane są ich
        //reprezentacje na ekranie
        for (TrafficLightsAndCrossing crossing : simulationController.getCrossings()) {
            terrainMainPanel.getChildren().add(crossing.getCrossingGraphics());
        }
        //Dla każdego z pomiarów odcinkowych, które zostały zainicjowane w kontrolerze symulacji, dodawane są ich
        //reprezentacje na ekranie. Obliczamy punkt początku i końca pomiaru odcinkowego i odpowiednio przesuwamy
        //go dla rozmiaru obrazu, aby wyśrodkować, a następnie w obu miejscach dodajemy obraz z fotoradarem.
        //Dodajemy również obraz z ograniczeniem prędkości na środku pomiędzy radarami
        if (simulationController.getSpeedRadars() != null && !simulationController.getSpeedRadars().isEmpty()) {
            terrainMainPanel.getChildren().addAll(simulationController.getSpeedRadars());
            for (int i = 0; i < simulationController.getSpeedRadars().size(); i+=2) {
                double x1 = simulationController.getSpeedRadars().get(i).getX();
                double x2 = simulationController.getSpeedRadars().get(i+1).getX();
                double lowerX = x1 < x2 ? x1 : x2;
                double biggerX = x1 > x2 ? x1 : x2;
                double x = lowerX + (biggerX - lowerX) / 2.0 + 10;
                terrainMainPanel.getChildren().add(new SpeedLimitSign(x, simulationController.getSpeedRadars().get(i).getY()));
            }
        }
        //Ustawiamy aktualny znak
        setWeatherSign(simulationController.getWeatherConditions());
    }

    /**
     * Metoda ustawiająca i zmieniająca znak z warunkami pogodowymi na scenie
     * @param weatherConditions Aktualne warunki pogodowe do wczytania
     */
    public void setWeatherSign(WeatherEnum weatherConditions) {
        //Usuwamy obecny znak z pogodą jeśli takowy się znajduje na scenie
        if (currentWeather != null && terrainMainPanel.getChildren().contains(currentWeather)) {
            terrainMainPanel.getChildren().remove(currentWeather);
        }
        //Wczytujemy obraz z odpowiednią pogodą z resource'ów
        Image img =
            new Image(getClass().getClassLoader().
                    getResourceAsStream("resources/images/weather/" + weatherConditions.getResourceName()));
        //Ustawiamy obraz, jego wymiary i położenie
        currentWeather = new ImageView(img);
        currentWeather.setPreserveRatio(true);
        currentWeather.setFitWidth(weatherOriginalImageWidth / weatherImageScale);
        currentWeather.setFitHeight(weatherOriginalImageHeight / weatherImageScale);
        currentWeather.setX(windowWidth / 2.0 - weatherOriginalImageWidth / weatherImageScale / 2.0);
        //Dodajemy obraz do sceny
        terrainMainPanel.getChildren().add(currentWeather);
    }

    /**
     * Metoda inicjalizująca i pokazująca ekran podsumowania działania aplikacji
     * @param resFilename Pliki z wynikami działania
     */
    public void showResultsScreen(List<String> resFilename) {
        Parent root;
        try {
            //Pobieramy plik z ekranem napisanym w xml-u i ładujemy go
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/screens/results.fxml"));
            root = loader.load();
            //Tworzymy sceneę, nadajemy tytuł okna i wymiary
            Stage stage = new Stage();
            stage.setTitle("Results screen");
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            //Dodajemy scenę do ekranu
            stage.setScene(scene);
            //Blokujemy możliwość ręcznej zmiany wielkości okna
            stage.setResizable(false);
            //Pobieramy kontroler ekranu podsumowania i wołamy metodę inicjalizującą go nazwą pliku
            ResultsController resultsController = loader.getController();
            resultsController.initController(resFilename);
            //Zamykamy okno symulacji i prezentujemy okno podsumowania
            ((Stage) terrainMainPanel.getScene().getWindow()).close();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
