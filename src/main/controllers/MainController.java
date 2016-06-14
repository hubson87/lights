package main.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import main.model.enums.AlgorithmType;
import main.model.enums.WeatherEnum;
import main.utils.NumberUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Krzysztof Baran
 * Klasa kontrolera ekranu głównego.
 * Jej zadaniem jest zebranie parametrów symulacji i utworzenie oraz uruchomienie ekranu symulacji
 */
public class MainController implements Initializable {
    /**
     * Szerokość ekranu symulacji do utworzenia
     */
    private static int TERRAIN_WIDTH = 1000;
    /**
     * Wysokość ekranu symulacji do utworzenia
     */
    private static int TERRAIN_HEIGHT = 600;
    /**
     * Przycisk generacji ekranu symulacji
     */
    @FXML
    private Button generateButton;
    /**
     * Slider zbierający informację o ilości pasów w pionie na pierwszym skrzyżowaniu
     */
    @FXML
    private Slider verticalBeltsCount;
    /**
     * Slider zbierający informację o ilości pasów w pionie na drugim skrzyżowaniu
     */
    @FXML
    private Slider verticalBelts2Count;
    /**
     * Slider zbierający informację o ilości pasów w poziomie
     */
    @FXML
    private Slider horizontalBeltsCount;
    /**
     * Slider zbierający informację o maksymalnej ilości samochodów na pasie
     */
    @FXML
    private Slider carsLimit;
    /**
     * Spinner zbierający informację o czasie trwania symulacji
     */
    @FXML
    private Spinner<Integer> timeSpinner;
    /**
     * Przycisk typu radio odpowiadający za słoneczną pogodę
     */
    @FXML
    private RadioButton sunnyRB;
    /**
     * Przycisk typu radio odpowiadający za deszczową pogodę
     */
    @FXML
    private RadioButton rainyRB;
    /**
     * Przycisk typu radio odpowiadający za śnieżną pogodę
     */
    @FXML
    private RadioButton snowyRB;
    /**
     * Przycisk typu radio odpowiadający za gołoledź
     */
    @FXML
    private RadioButton glazeRB;
    /**
     * Przycisk typu radio odpowiadający za mglistą pogodę
     */
    @FXML
    private RadioButton foggyRB;
    /**
     * Przycisk typu radio odpowiadający za iterowanie każdej pogody po kolei w stałym czasie
     */
    @FXML
    private RadioButton allWeatherRB;
    /**
     * Przycisk typu radio odpowiadający za algorytm zmieniający światła w stałym czasie
     */
    @FXML
    private RadioButton fixedTimeRB;

    /**
     * Metoda wołana podczas tworzenia MainControllera przez framework JavaFX
     * @param location Lokacja względem której możemy ustalać względne ścieżki (ignorowana na tym etapie)
     * @param resources Obiekt za pomocą którego możemy pobierać resource aplikacji (ignorowany na tym etapie)
     */
    @Override
    public void initialize(URL location, final ResourceBundle resources) {
        //Ustawiamy słoneczą pogodę jako domyślnie zaznaczoną
        sunnyRB.setSelected(true);
        //Ustawiamy algorytm zmiany świateł przy stałym czasie jako domyślnie zaznaczony
        fixedTimeRB.setSelected(true);
        //Patch na spinnera, który pozwala wpisywać wartości ręcznie.
        //Zakładamy listenera eventów na pole tekstowe i jeśli wpisana wartość jest typu int,
        //to aktualizujemy wartość spinnera
        timeSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (NumberUtils.isInteger(newValue)) {
                    timeSpinner.getValueFactory().setValue(Integer.parseInt(newValue));
                }
            } catch (NumberFormatException e) {
                if (NumberUtils.isInteger(oldValue)) {
                    timeSpinner.getValueFactory().setValue(Integer.parseInt(oldValue));
                }
            }
        });
        //Kod obsługujący kliknięcie przycisku generacji ekranu symulacji
        generateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    //Pobieramy plik z ekranem napisanym w xml-u i ładujemy go
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/screens/terrain.fxml"));
                    root = loader.load();
                    //Tworzymy sceneę, nadajemy tytuł okna i wymiary
                    Stage stage = new Stage();
                    stage.setTitle("TrafficLights - terrain");
                    Scene scene = new Scene(root, TERRAIN_WIDTH, TERRAIN_HEIGHT);
                    //Wczytujemy i ustawiamy style ekranu symulacji, dodajemy scenę do ekranu
                    scene.getStylesheets().addAll(this.getClass().getResource("/resources/css/terrainStyles.css ").toExternalForm());
                    stage.setScene(scene);
                    //Blokujemy możliwość ręcznej zmiany wielkości okna
                    stage.setResizable(false);
                    //Pobieramy kontroler ekranu symulacji i wołamy metodę inicjalizującą go wybranymi przez użytkownika
                    //na głównym ekranie parametrami i wartościami
                    TerrainController terrainController = loader.getController();
                    terrainController.initControllerValues(
                            getWeatherFromRadioButtons(),
                            getAlgorithmFromRadioButtons(),
                            (int)verticalBeltsCount.getValue(),
                            (int)verticalBelts2Count.getValue(),
                            (int)horizontalBeltsCount.getValue(),
                            (int)carsLimit.getValue(),
                            timeSpinner.getValue(),
                            TERRAIN_WIDTH, TERRAIN_HEIGHT);
                    //Zamykamy okno główne i prezentujemy okno symulacji
                    ((Stage)generateButton.getScene().getWindow()).close();
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Metoda pobierająca enumerator algorytmu w zależności od wybranej wartości na ekranie przez użytkownika
     * @return Enumerator z wybranym przez użytkownika typem algorytmu
     */
    private AlgorithmType getAlgorithmFromRadioButtons() {
        //Jeśli algorytm w stałym czasie jest wybrany, to zwracamy enumerator ze stałym czasem,
        //wpp enumerator ilości samochodów
        return fixedTimeRB.isSelected() ? AlgorithmType.FIXED_TIME : AlgorithmType.CARS_COUNT;
    }

    /**
     * Metoda pobierająca enumerator pogody w zależności od wybranej wartośći na ekranie przez użytkownika
     * @return Enumerator pogody wybrany przez użytkownika. Jeśli pogoda losowa => null
     */
    private WeatherEnum getWeatherFromRadioButtons() {
        if (sunnyRB.isSelected()) {
            return WeatherEnum.SUNNY;
        } else if (rainyRB.isSelected()) {
            return WeatherEnum.RAINY;
        } else if (snowyRB.isSelected()) {
            return WeatherEnum.SNOWY;
        } else if (glazeRB.isSelected()) {
            return WeatherEnum.GLAZE;
        } else if (foggyRB.isSelected()) {
            return WeatherEnum.FOGGY;
        } else if (allWeatherRB.isSelected()) {
            return WeatherEnum.ALL;
        }
        return null;
    }
}
