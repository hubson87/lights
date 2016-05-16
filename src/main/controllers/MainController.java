package main.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static int TERRAIN_WIDTH = 1000;
    private static int TERRAIN_HEIGHT = 600;
    @FXML
    private Button generateButton;
    @FXML
    private Slider verticalBeltsCount;
    @FXML
    private Slider verticalBelts2Count;
    @FXML
    private Slider horizontalBeltsCount;
    @FXML
    private Slider carsLimit;


    @Override
    public void initialize(URL location, final ResourceBundle resources) {
        generateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/screens/terrain.fxml"));
                    root = loader.load();
                    Stage stage = new Stage();
                    stage.setTitle("TrafficLights - terrain");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    Scene scene = new Scene(root, TERRAIN_WIDTH, TERRAIN_HEIGHT);
                    scene.getStylesheets().addAll(this.getClass().getResource("/resources/css/terrainStyles.css ").toExternalForm());
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.setAlwaysOnTop(true);
                    TerrainController terrainController = loader.getController();
                    terrainController.initControllerValues(
                            (int)verticalBeltsCount.getValue(),
                            (int)verticalBelts2Count.getValue(),
                            (int)horizontalBeltsCount.getValue(),
                            (int)carsLimit.getValue(),
                            TERRAIN_WIDTH, TERRAIN_HEIGHT);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
