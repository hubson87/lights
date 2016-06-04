package main.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Created by Krzysztof Baran
 * Kontroler ekranu wyników działania aplikacji
 * Prezentuje nazwę pliku w którymm zapisane są wyniki symulacji oraz pozwala wrócić do ekranu głównego po zakończeniu
 * działania aplikacji
 */
public class ResultsController implements Initializable {
    /**
     * Labelka trzymająca tekst podsumowania działania aplikacji
     */
    @FXML
    private Label summaryLabel;
    /**
     * Przycisk pozwalający na powrót do ekranu głównego symulacji i rozpoczęcie jej od nowa
     */
    @FXML
    private Button goBackBtn;

    /**
     * Szerokość ekranu głównego
     */
    private static int WINDOW_WIDTH = 800;
    /**
     * Wysokość ekranu głównego
     */
    private static int WINDOW_HEIGHT = 400;

    /**
     * Metoda inicjalizująca kontroler, wołana przez framework JavaFX
     * @param location Lokacja względem której możemy ustalać względne ścieżki (ignorowana na tym etapie)
     * @param resources Obiekt za pomocą którego możemy pobierać resource aplikacji (ignorowany na tym etapie)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Ustawiamy działanie przycisku powrotu do ekranu głównego
        goBackBtn.setOnAction(event -> {
            Parent root;
            try {
                //Pobieramy plik z ekranem napisanym w xml-u i ładujemy go
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/screens/mainScreen.fxml"));
                root = loader.load();
                //Tworzymy sceneę, nadajemy tytuł okna i wymiary
                Stage stage = new Stage();
                stage.setTitle("TrafficLights - terrain");
                Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
                //Dodajemy scenę do ekranu
                stage.setScene(scene);
                //Blokujemy możliwość ręcznej zmiany wielkości okna
                stage.setResizable(false);
                //Zamykamy ekran podsumowania i pokazujemy ekran główny
                ((Stage)goBackBtn.getScene().getWindow()).close();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Metoda inicjalizuąca kontroler. Przekazywana jest do niej nazwa pliku z wynikami do wyświetlenia,
     * która ustawiana jest w labelce z podsumowaniem
     * @param resFilename Nazwa pliku z wynikami
     */
    public void initController(String resFilename) {
        summaryLabel.setText("Summary in file: " + resFilename + "\n");
    }

}
