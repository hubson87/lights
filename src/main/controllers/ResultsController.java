package main.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class ResultsController implements Initializable {
    @FXML
    private Label summaryLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initController(String resFilename) {
        summaryLabel.setText("Summary in file: " + resFilename + "\n");
    }

}
