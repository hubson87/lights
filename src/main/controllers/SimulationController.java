package main.controllers;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import main.model.TrafficBelt;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.DirectionEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SimulationController {
    private List<TrafficBelt> verticalBelts;
    private List<TrafficBelt> verticalBelts2;
    private List<TrafficBelt> horizontalBelts;
    private List<TrafficLightsAndCrossing> crossings;
    private int simulationTime;
    private final Random random = new Random();

    public SimulationController(int verticalBeltsCount, int verticalBelts2Count, int horizontalBeltsCount,
                                int carsLimit, int simulationTime, int width, int height) {

        verticalBelts = initVerticalBelts(verticalBeltsCount, height, width, carsLimit, width / 4);
        verticalBelts2 = initVerticalBelts(verticalBelts2Count, height, width, carsLimit, 3 * width / 4);
        horizontalBelts = initHorizontalBelts(horizontalBeltsCount, height, width, carsLimit);
        crossings = initCrossings(verticalBeltsCount, verticalBelts2Count, horizontalBeltsCount, height, width);    //first and the second crossing
        for (TrafficBelt belt : horizontalBelts) {
            belt.getCrossingAndLights().add(crossings.get(0));
            belt.getCrossingAndLights().add(crossings.get(1));
        }
        for (TrafficBelt belt : verticalBelts) {
            belt.getCrossingAndLights().add(crossings.get(0));
        }
        for (TrafficBelt belt : verticalBelts2) {
            belt.getCrossingAndLights().add(crossings.get(1));
        }
        this.simulationTime = simulationTime;

    }

    public void startSimulation(final TerrainController terrainController) {
        final int taskPeriod = 30;
        final double[] interval = { (double) simulationTime * 1000.0 / (double) taskPeriod };
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                simulationIteration(terrainController);
                synchronized (interval) {
                    if (--interval[0] <= 1) {
                        timer.cancel();
                    }
                }
            }
        }, 1000, taskPeriod);
        Timer timer2 = new Timer(true);
        timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (TrafficLightsAndCrossing cross : crossings) {
                    synchronized (interval) {
                        if (interval[0] <= 1) {
                            timer2.cancel();
                            return;
                        }
                    }
                    cross.changeLights();
                }
            }
        }, 3000, 6000);
    }

    private void simulationIteration(final TerrainController terrainController) {
        Platform.runLater(() -> {
            for (TrafficBelt belt : SimulationController.this.getAllBelts()) {
                terrainController.removeCarsFromStage(belt.moveCars());
                ImageView res = belt.addCar(randomMaxSpeedForCar());
                if (res != null) {
                    terrainController.addCarOnStage(res);
                }
            }
        });
    }

    private synchronized int randomMaxSpeedForCar() {
        if (random.nextInt(10) == 1) {
            return new Random().nextInt(40) + 40;
        }
        return random.nextInt(20) + 10;
    }

    private List<TrafficLightsAndCrossing> initCrossings(int verticalBeltsCount, int verticalBelts2Count,
                                                         int horizontalBeltsCount, int windowHeight, int windowWidth) {
        List<TrafficLightsAndCrossing> res = new ArrayList<TrafficLightsAndCrossing>();
        int yFrom =  windowHeight / 2 - horizontalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;
        int yTo =  windowHeight / 2 + horizontalBeltsCount * TrafficBelt.BELT_HEIGHT + 3;

        int x1From = windowWidth / 4 - verticalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;
        int x1To = windowWidth / 4 + verticalBeltsCount * TrafficBelt.BELT_HEIGHT + 3;

        int x2From = 3 * windowWidth / 4 - verticalBelts2Count * TrafficBelt.BELT_HEIGHT - 3;
        int x2To = 3 * windowWidth / 4 + verticalBelts2Count * TrafficBelt.BELT_HEIGHT + 3;

        res.add(new TrafficLightsAndCrossing(x1From, x1To, yFrom, yTo));
        res.add(new TrafficLightsAndCrossing(x2From, x2To, yFrom, yTo));
        return res;
    }

    private List<TrafficBelt> initVerticalBelts(int verticalBeltsCount, int windowHeight, int windowWidth, int carsLimit, int offset) {
        List<TrafficBelt> res = new ArrayList<TrafficBelt>();
        int beltsXStart = offset - verticalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;   //we want to separate them
        for (int i = 0; i < verticalBeltsCount; i++) {
            TrafficBelt belt = new TrafficBelt(carsLimit, beltsXStart, 0, TrafficBelt.BELT_HEIGHT, windowHeight + 20, DirectionEnum.DOWN);
            res.add(belt);
            beltsXStart += TrafficBelt.BELT_HEIGHT;
        }
        beltsXStart += 6;
        for (int i = 0; i < verticalBeltsCount; i++) {
            TrafficBelt belt = new TrafficBelt(carsLimit, beltsXStart, 0, TrafficBelt.BELT_HEIGHT, windowHeight + 20, DirectionEnum.UP);
            res.add(belt);
            beltsXStart += TrafficBelt.BELT_HEIGHT;
        }
        return res;
    }

    public List<TrafficBelt> initHorizontalBelts(int horizontalBeltsCount, int windowHeight, int windowWidth, int carsLimit) {
        List<TrafficBelt> res = new ArrayList<TrafficBelt>();
        int beltsYStart = windowHeight / 2 - horizontalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;   //we want to separate them
        for (int i = 0; i < horizontalBeltsCount; i++) {
            TrafficBelt belt = new TrafficBelt(carsLimit, 0, beltsYStart, windowWidth + 20, TrafficBelt.BELT_HEIGHT, DirectionEnum.RIGHT);
            res.add(belt);
            beltsYStart += TrafficBelt.BELT_HEIGHT;
        }
        beltsYStart += 6;
        for (int i = 0; i < horizontalBeltsCount; i++) {
            TrafficBelt belt = new TrafficBelt(carsLimit, 0, beltsYStart, windowWidth + 20, TrafficBelt.BELT_HEIGHT, DirectionEnum.LEFT);
            res.add(belt);
            beltsYStart += TrafficBelt.BELT_HEIGHT;
        }
        return res;
    }

    public List<TrafficBelt> getAllBelts() {
        List<TrafficBelt> res = new ArrayList<TrafficBelt>();
        res.addAll(horizontalBelts);
        res.addAll(verticalBelts);
        res.addAll(verticalBelts2);
        return res;
    }

    public List<TrafficLightsAndCrossing> getCrossings() {
        return crossings;
    }
}
