package main.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import main.model.TrafficBelt;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.WeatherEnum;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TerrainController implements Initializable {
    @FXML
    Pane terrainMainPanel;
    @FXML
    Button startSimulationButton;

    private SimulationController simulationController;
    private ImageView currentWeather;
    private double windowWidth, windowHeight;
    private final double weatherOriginalImageWidth = 801.0;
    private final double weatherOriginalImageHeight = 323.0;
    private final double weatherImageScale = 6.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startSimulationButton.setOnAction(event -> {
            if (simulationController == null) {
                return;
            }
            TasksHandler.runTask(new Task() {
                @Override
                protected Object call() throws Exception {
                    simulationController.startSimulation(TerrainController.this);
                    Platform.runLater(() -> startSimulationButton.setDisable(true));
                    return null;
                }
            });
        });
    }

    public void addCarOnStage(ImageView car) {
        terrainMainPanel.getChildren().add(car);
    }

    public void removeCarsFromStage(List<ImageView> cars) {
        if (cars != null && !cars.isEmpty()) {
            Platform.runLater(() -> terrainMainPanel.getChildren().removeAll(cars));
        }
    }

    public void initControllerValues(WeatherEnum weatherConditions, int verticalBeltsCount, int verticalBelts2Count, int horizontalBeltsCount,
                                     int carsLimit, int simulationTime, int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        simulationController = new SimulationController(weatherConditions, verticalBeltsCount, verticalBelts2Count, horizontalBeltsCount,
                carsLimit, simulationTime, width, height);

        for (TrafficBelt belt : simulationController.getAllBelts()) {
            terrainMainPanel.getChildren().add(belt.getBeltGraphics());
        }

        for (TrafficLightsAndCrossing crossing : simulationController.getCrossings()) {
            terrainMainPanel.getChildren().add(crossing.getCrossingGraphics());
        }
    }

    public void setWeatherSign(WeatherEnum weatherConditions) {
        if (currentWeather != null && terrainMainPanel.getChildren().contains(currentWeather)) {
            terrainMainPanel.getChildren().remove(currentWeather);
        }
        Image img = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/weather/" + weatherConditions.getResourceName()));
        currentWeather = new ImageView(img);
        currentWeather.setPreserveRatio(true);
        currentWeather.setFitWidth(weatherOriginalImageWidth / weatherImageScale);
        currentWeather.setFitHeight(weatherOriginalImageHeight / weatherImageScale);
        currentWeather.setX(windowWidth / 2.0 - weatherOriginalImageWidth / weatherImageScale / 2.0);
        terrainMainPanel.getChildren().add(currentWeather);
    }

    public void showResultsScreen(String resFilename) {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/screens/results.fxml"));
            root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Results screen");
            Scene scene = new Scene(root, windowWidth, windowHeight);
            stage.setScene(scene);
            stage.setResizable(false);
            ResultsController resultsController = loader.getController();
            resultsController.initController(resFilename);
            ((Stage)terrainMainPanel.getScene().getWindow()).close();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
