package main.controllers;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import main.model.TrafficBelt;
import main.model.TrafficLightsAndCrossing;
import main.model.enums.DirectionEnum;
import main.model.enums.WeatherEnum;
import main.utils.ExcelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SimulationController {
    private List<TrafficBelt> verticalBelts;
    private List<TrafficBelt> verticalBelts2;
    private List<TrafficBelt> horizontalBelts;
    private List<TrafficLightsAndCrossing> crossings;
    private int simulationTime;
    private WeatherEnum weatherConditions;
    private final boolean dynamicWeather;

    public SimulationController(WeatherEnum weatherConditions, int verticalBeltsCount, int verticalBelts2Count, int horizontalBeltsCount,
                                int carsLimit, int simulationTime, int width, int height) {

        this.dynamicWeather = weatherConditions == null;
        this.weatherConditions = weatherConditions == null ? WeatherEnum.getRandom() : weatherConditions;
        verticalBelts = initVerticalBelts(verticalBeltsCount, height, width, carsLimit, width / 4);
        verticalBelts2 = initVerticalBelts(verticalBelts2Count, height, width, carsLimit, 3 * width / 4);
        horizontalBelts = initHorizontalBelts(horizontalBeltsCount, height, width, carsLimit, verticalBeltsCount, verticalBelts2Count);
        crossings = initCrossings(verticalBeltsCount, verticalBelts2Count, horizontalBeltsCount, height, width);    //first and the second crossing
        for (TrafficBelt belt : horizontalBelts) {
            belt.setStoppingDistFact(this.weatherConditions.getStoppingDistanceFactor());
            belt.getCrossingAndLights().add(crossings.get(0));
            belt.getCrossingAndLights().add(crossings.get(1));
        }
        for (TrafficBelt belt : verticalBelts) {
            belt.setStoppingDistFact(this.weatherConditions.getStoppingDistanceFactor());
            belt.getCrossingAndLights().add(crossings.get(0));
        }
        for (TrafficBelt belt : verticalBelts2) {
            belt.setStoppingDistFact(this.weatherConditions.getStoppingDistanceFactor());
            belt.getCrossingAndLights().add(crossings.get(1));
        }
        this.simulationTime = simulationTime;

    }

    public void startSimulation(final TerrainController terrainController) {
        final int taskPeriod = 30;
        final double[] interval = {(double) simulationTime * 1000.0 / (double) taskPeriod};
        Timer timer = new Timer(true);
        Platform.runLater(() -> terrainController.setWeatherSign(weatherConditions));
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                simulationIteration(terrainController);
                synchronized (interval) {
                    if (--interval[0] <= 1) {
                        timer.cancel();
                        String resFilename = collectResultsToFiles();
                        Platform.runLater(() -> terrainController.showResultsScreen(resFilename));
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
        //change weather dynamically
        if (dynamicWeather) {
            Timer weatherTimer = new Timer(true);
            weatherTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    synchronized (weatherConditions) {
                        weatherConditions = WeatherEnum.getRandom();
                        Platform.runLater(() -> terrainController.setWeatherSign(weatherConditions));
                        TrafficBelt.setStoppingDistFact(weatherConditions.getStoppingDistanceFactor());
                        for (TrafficBelt belt : getAllBelts()) {
                            belt.changeCarsSpeed(weatherConditions);
                        }
                    }
                }
            }, 6000, 18000);
        }
    }

    private String collectResultsToFiles() {
        return ExcelUtils.exportSpeeds(getAllBelts());
    }

    private void simulationIteration(final TerrainController terrainController) {
        Platform.runLater(() -> {
            for (TrafficBelt belt : SimulationController.this.getAllBelts()) {
                terrainController.removeCarsFromStage(belt.moveCars());
                ImageView res = belt.addCar(weatherConditions);
                if (res != null) {
                    terrainController.addCarOnStage(res);
                }
            }
        });
    }

    private List<TrafficLightsAndCrossing> initCrossings(int verticalBeltsCount, int verticalBelts2Count,
                                                         int horizontalBeltsCount, int windowHeight, int windowWidth) {
        List<TrafficLightsAndCrossing> res = new ArrayList<TrafficLightsAndCrossing>();
        int yFrom = windowHeight / 2 - horizontalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;
        int yTo = windowHeight / 2 + horizontalBeltsCount * TrafficBelt.BELT_HEIGHT + 3;

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
            TrafficBelt belt = new TrafficBelt(carsLimit, beltsXStart, 0, TrafficBelt.BELT_HEIGHT, windowHeight + 20, DirectionEnum.DOWN, null, null);
            res.add(belt);
            beltsXStart += TrafficBelt.BELT_HEIGHT;
        }
        beltsXStart += 6;
        for (int i = 0; i < verticalBeltsCount; i++) {
            TrafficBelt belt = new TrafficBelt(carsLimit, beltsXStart, 0, TrafficBelt.BELT_HEIGHT, windowHeight + 20, DirectionEnum.UP, null, null);
            res.add(belt);
            beltsXStart += TrafficBelt.BELT_HEIGHT;
        }
        return res;
    }

    public List<TrafficBelt> initHorizontalBelts(int horizontalBeltsCount, int windowHeight, int windowWidth, int carsLimit, int verticalBeltsCount, int verticalBelts2Count) {
        List<TrafficBelt> res = new ArrayList<TrafficBelt>();
        int rightBeltSpeedControlStart = (int)((double)windowWidth / 4.0 +
                ((double)verticalBeltsCount + 1.0) * (double)TrafficBelt.BELT_HEIGHT + (double)verticalBeltsCount * 3.0);
        int rightBeltSpeedControlEnd = (int)((double)rightBeltSpeedControlStart + (double)windowWidth / 8.0);
        int leftBeltSpeedControlStart = (int)(3.0 * (double)windowWidth / 4.0 -
                ((double)verticalBeltsCount + 1.0) * (double)TrafficBelt.BELT_HEIGHT - (double)verticalBeltsCount * 3.0);
        int leftBeltSpeedControlEnd = (int)((double)rightBeltSpeedControlStart - (double)windowWidth / 8.0);
        int beltsYStart = windowHeight / 2 - horizontalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;   //we want to separate them
        for (int i = 0; i < horizontalBeltsCount; i++) {
            TrafficBelt belt = new TrafficBelt(carsLimit, 0, beltsYStart, windowWidth + 20, TrafficBelt.BELT_HEIGHT,
                    DirectionEnum.LEFT, leftBeltSpeedControlStart, leftBeltSpeedControlEnd);
            res.add(belt);
            beltsYStart += TrafficBelt.BELT_HEIGHT;
        }
        beltsYStart += 6;
        for (int i = 0; i < horizontalBeltsCount; i++) {
            TrafficBelt belt = new TrafficBelt(carsLimit, 0, beltsYStart, windowWidth + 20, TrafficBelt.BELT_HEIGHT,
                    DirectionEnum.RIGHT, rightBeltSpeedControlStart, rightBeltSpeedControlEnd);
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
