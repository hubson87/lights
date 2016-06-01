package main.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import main.model.TrafficBelt;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.WeatherEnum;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TerrainController implements Initializable {
    @FXML
    Pane terrainMainPanel;
    @FXML
    Button startSimulationButton;

    private int carsOnBeltLimit;
    private SimulationController simulationController;

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
        carsOnBeltLimit = carsLimit;
        simulationController = new SimulationController(weatherConditions, verticalBeltsCount, verticalBelts2Count, horizontalBeltsCount,
                carsLimit, simulationTime, width, height);

        for (TrafficBelt belt : simulationController.getAllBelts()) {
            terrainMainPanel.getChildren().add(belt.getBeltGraphics());
        }

        for (TrafficLightsAndCrossing crossing : simulationController.getCrossings()) {
            terrainMainPanel.getChildren().add(crossing.getCrossingGraphics());
        }
    }

}
