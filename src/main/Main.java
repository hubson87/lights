package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Krzysztof Baran
 * Klasa główna, która ma za zadanie wystartować aplikację.
 * Klasa rozszerza Application, która jest klasą aplikacji JavaFX
 */
public class Main extends Application {

    /**
     * Funkcja uruchamiana przy starcie aplikacji, wczytuje ekran mainScreen,
     * ustawia tytuł ekranu na TrafficLights - main, rozmiar na 800x600 oraz wyświetla scenę
     * @param primaryStage Parametr sceny, która może zostać ustawiona
     * @throws Exception Wyjątek, który może zostać rzucony podczas wczytywania resource'a itp...
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/resources/screens/mainScreen.fxml"));
        primaryStage.setTitle("TrafficLights - main");
        primaryStage.setScene(new Scene(root, 800, 400));
        primaryStage.show();
    }

    /**
     * Funckja główna, która startuje aplikację
     * @param args Argumenty aplikacji (ignorowane na tym etapie)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
