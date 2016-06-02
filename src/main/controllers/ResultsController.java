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

public class ResultsController implements Initializable {
    @FXML
    private Label summaryLabel;
    @FXML
    private Button goBackBtn;

    private static int WINDOW_WIDTH = 800;
    private static int WINDOW_HEIGHT = 400;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        goBackBtn.setOnAction(event -> {
            Parent root;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/screens/mainScreen.fxml"));
                root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("TrafficLights - terrain");
                Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
                scene.getStylesheets().addAll(this.getClass().getResource("/resources/css/terrainStyles.css ").toExternalForm());
                stage.setScene(scene);
                stage.setResizable(false);
                ((Stage)goBackBtn.getScene().getWindow()).close();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void initController(String resFilename) {
        summaryLabel.setText("Summary in file: " + resFilename + "\n");
    }

}
