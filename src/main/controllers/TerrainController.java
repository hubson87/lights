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
import main.controllers.handlers.TasksHandler;
import main.model.SpeedLimitSign;
import main.model.belts.TrafficBelt;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.AlgorithmType;
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
    private static int WINDOW_WIDTH = 800;
    private static int WINDOW_HEIGHT = 600;

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
            new Thread(new Task() {
                @Override
                protected Object call() throws Exception {
                    simulationController.startSimulation(TerrainController.this);
                    Platform.runLater(() -> startSimulationButton.setDisable(true));
                    return null;
                }
            }).start();
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

    public void initControllerValues(WeatherEnum weatherConditions, AlgorithmType algorithmType, int verticalBeltsCount,
                                     int verticalBelts2Count, int horizontalBeltsCount,
                                     int carsLimit, int simulationTime, int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        simulationController = new SimulationController(weatherConditions, algorithmType, verticalBeltsCount, verticalBelts2Count, horizontalBeltsCount,
            carsLimit, simulationTime, width, height);

        for (TrafficBelt belt : simulationController.getAllBelts()) {
            terrainMainPanel.getChildren().add(belt.getBeltGraphics());
        }

        for (TrafficLightsAndCrossing crossing : simulationController.getCrossings()) {
            terrainMainPanel.getChildren().add(crossing.getCrossingGraphics());
        }

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
        setWeatherSign(simulationController.getWeatherConditions());
    }

    public void setWeatherSign(WeatherEnum weatherConditions) {
        if (currentWeather != null && terrainMainPanel.getChildren().contains(currentWeather)) {
            terrainMainPanel.getChildren().remove(currentWeather);
        }
        Image img =
            new Image(getClass().getClassLoader().getResourceAsStream("resources/images/weather/" + weatherConditions.getResourceName()));
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
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setResizable(false);
            ResultsController resultsController = loader.getController();
            resultsController.initController(resFilename);
            ((Stage) terrainMainPanel.getScene().getWindow()).close();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
