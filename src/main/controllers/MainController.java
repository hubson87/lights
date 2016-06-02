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
    @FXML
    private Spinner<Integer> timeSpinner;
    @FXML
    private RadioButton sunnyRB;
    @FXML
    private RadioButton rainyRB;
    @FXML
    private RadioButton snowyRB;
    @FXML
    private RadioButton glazeRB;
    @FXML
    private RadioButton foggyRB;
    @FXML
    private RadioButton fixedTimeRB;

    @Override
    public void initialize(URL location, final ResourceBundle resources) {
        sunnyRB.setSelected(true);
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

        generateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/screens/terrain.fxml"));
                    root = loader.load();
                    Stage stage = new Stage();
                    stage.setTitle("TrafficLights - terrain");
                    Scene scene = new Scene(root, TERRAIN_WIDTH, TERRAIN_HEIGHT);
                    scene.getStylesheets().addAll(this.getClass().getResource("/resources/css/terrainStyles.css ").toExternalForm());
                    stage.setScene(scene);
                    stage.setResizable(false);
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
                    ((Stage)generateButton.getScene().getWindow()).close();
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private AlgorithmType getAlgorithmFromRadioButtons() {
        return fixedTimeRB.isSelected() ? AlgorithmType.FIXED_TIME : AlgorithmType.CARS_COUNT;
    }

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
        }
        return null;
    }
}
