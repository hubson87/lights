package main.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import main.model.TrafficBelt;
import main.model.TrafficLightsAndCrossing;

import java.net.URL;
import java.util.ResourceBundle;

public class TerrainController implements Initializable {
    @FXML
    Pane terrainMainPanel;
    @FXML
    Button startSimulationButton;

    private SimulationController simulationController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startSimulationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (simulationController == null) {
                    return;
                }
            }
        });
    }

    public void initControllerValues(int verticalBeltsCount, int verticalBelts2Count, int horizontalBeltsCount,
                                    int carsLimit, int width, int height) {
        simulationController = new SimulationController(verticalBeltsCount, verticalBelts2Count, horizontalBeltsCount,
                carsLimit, width, height);

        for (TrafficBelt belt : simulationController.getAllBelts()) {
            terrainMainPanel.getChildren().add(belt.getBeltGraphics());
        }

        for (TrafficLightsAndCrossing crossing : simulationController.getCrossings()) {
            terrainMainPanel.getChildren().add(crossing.getCrossingGraphics());
        }

        TasksHandler.runTask(new Task() {
            @Override
            protected Object call() throws Exception {
                simulationController.getCrossings().get(0).changeLights();
                Thread.sleep(4000);
                simulationController.getCrossings().get(0).changeLights();
                return null;
            }
        });
    }

}
