package main.model;

import com.sun.javafx.geom.Rectangle;

public class TrafficLightsAndCrossing {
    private Rectangle crossingCoordinates;
    private boolean horizontalGreen;
    private boolean verticalGreen;

    public TrafficLightsAndCrossing(Rectangle crossingCoordinates, boolean horizontalGreen) {
        this.crossingCoordinates = crossingCoordinates;
        this.horizontalGreen = horizontalGreen;
        this.verticalGreen = !horizontalGreen;
    }

    //we need one second to let all cars break before letting the others to start
    public void changeLights() {
        new Runnable() {
            @Override
            public void run() {
                if (horizontalGreen) {
                    horizontalGreen = false;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        verticalGreen = true;
                    }
                } else {
                    verticalGreen = false;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        horizontalGreen = true;
                    }
                }
            }
        }.run();
    }
}
