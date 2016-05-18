package main.model;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.controllers.TasksHandler;

public class TrafficLightsAndCrossing {
    private ImageView lightUp, lightDown;
    private ImageView lightLeft, lightRight;
    private Image redLight, yellowLight, greenLight;
    private Pane crossingCoordinates;
    private boolean horizontalGreen;
    private boolean verticalGreen;
    private int x1, x2, y1, y2;
    private int imageSizeX = 14;
    private int imageSizeY = 14;

    public TrafficLightsAndCrossing(int x1, int x2, int y1, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.crossingCoordinates = initPane(x1, x2, y1, y2);
        this.redLight = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/red.png"));
        this.yellowLight = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/yellow.png"));
        this.greenLight = new Image(getClass().getClassLoader().getResourceAsStream("resources/images/green.png"));
        this.horizontalGreen = true;
        this.verticalGreen = false;
        setLights(redLight, greenLight);
    }

    private Pane initPane(int x1, int x2, int y1, int y2) {
        Pane res = new Pane();
        res.setPrefSize(x2-x1, y2-y1);
        res.setMinSize(x2 - x1, y2 - y1);
        res.setMaxSize(x2 - x1, y2 - y1);
        res.relocate(x1, y1);
        res.setStyle("-fx-background-color: #34495e;");
        return res;
    }

    private void setLights(Image vertical, Image horizontal) {
        crossingCoordinates.getChildren().clear();
        setVerticalLights(vertical);
        setHorizontalLights(horizontal);
        crossingCoordinates.getChildren().addAll(lightLeft, lightUp, lightRight, lightDown);
    }

    private void setVerticalLights(Image img) {
        this.lightUp = new ImageView(img);
        this.lightUp.relocate((x2 - x1) / 2 - imageSizeX / 2, 0);
        this.lightDown = new ImageView(img);
        this.lightDown.relocate((x2 - x1) / 2 - imageSizeX / 2, y2 - y1 - imageSizeY);
    }

    private void setHorizontalLights(Image img) {
        this.lightLeft = new ImageView(img);
        this.lightLeft.relocate(0, (y2 - y1) / 2 - imageSizeY / 2);
        this.lightRight = new ImageView(img);
        this.lightRight.relocate(x2 - x1 - imageSizeX, (y2 - y1) / 2 - imageSizeY / 2);
    }

    //we need one second to let all cars break before letting the others to start
    //TODO: http://stackoverflow.com/questions/20497845/constantly-update-ui-in-java-fx-worker-thread
    public void changeLights() {
        TasksHandler.runTask(new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(() -> {
                    if (horizontalGreen) {
                        horizontalGreen = false;
                        setLights(redLight, yellowLight);
                        switchLightsInThread(greenLight, redLight, true);
                    } else {
                        verticalGreen = false;
                        setLights(yellowLight, redLight);
                        switchLightsInThread(redLight, greenLight, false);
                    }
                });
                return null;
            }
        });
    }

    private void switchLightsInThread(Image vertical, Image horizontal, boolean newVerticalGreenValue) {
        TasksHandler.runTask(new Task() {
            @Override
            protected Object call() throws Exception {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Platform.runLater(() -> {
                            setLights(vertical, horizontal);
                            verticalGreen = newVerticalGreenValue;
                            horizontalGreen = !newVerticalGreenValue;
                        });
                    }
                return null;
            }
        });
    }

    public Pane getCrossingGraphics() {
        return crossingCoordinates;
    }

    public synchronized boolean isHorizontalGreen() {
        return horizontalGreen;
    }

    public synchronized boolean isVerticalGreen() {
        return verticalGreen;
    }
}
