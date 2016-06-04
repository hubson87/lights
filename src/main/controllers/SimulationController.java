package main.controllers;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import main.model.belts.DownTrafficBelt;
import main.model.belts.LeftTrafficBelt;
import main.model.belts.RightTrafficBelt;
import main.model.SpeedRadar;
import main.model.belts.TrafficBelt;
import main.model.TrafficLightsAndCrossing;
import main.model.belts.UpTrafficBelt;
import main.model.enums.AlgorithmType;
import main.model.enums.WeatherEnum;
import main.utils.DateUtils;
import main.utils.ExcelUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class SimulationController {
    private final AlgorithmType algorithmType;
    private List<TrafficBelt> verticalBelts;
    private List<TrafficBelt> verticalBelts2;
    private List<TrafficBelt> horizontalBelts;
    private List<TrafficLightsAndCrossing> crossings;
    private List<SpeedRadar> speedRadars;
    private int simulationTime;
    private WeatherEnum weatherConditions;
    private List<WeatherEnum> weatherConditionsList;
    private final boolean dynamicWeather;

    public SimulationController(WeatherEnum weatherConditions, AlgorithmType algorithmType, int verticalBeltsCount, int verticalBelts2Count,
                                int horizontalBeltsCount,
                                int carsLimit, int simulationTime, int width, int height) {

        this.dynamicWeather = weatherConditions == null;
        this.weatherConditions = weatherConditions == null ? WeatherEnum.getRandom() : weatherConditions;
        weatherConditionsList = new ArrayList<>();
        weatherConditionsList.add(this.weatherConditions);
        verticalBelts = initVerticalBelts(verticalBeltsCount, height, width, carsLimit, width / 4);
        verticalBelts2 = initVerticalBelts(verticalBelts2Count, height, width, carsLimit, 3 * width / 4);
        horizontalBelts = initHorizontalBelts(horizontalBeltsCount, height, width, carsLimit, verticalBeltsCount, verticalBelts2Count);
        crossings =
            initCrossings(verticalBeltsCount, verticalBelts2Count, horizontalBeltsCount, height, width);    //first and the second crossing
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
        this.algorithmType = algorithmType;

    }

    public void startSimulation(final TerrainController terrainController) {
        final int taskPeriod = 30;
        final double[] interval = { (double) simulationTime * 1000.0 / (double) taskPeriod };
        Timer timer = new Timer(true);
        final LocalDateTime iterationStart = LocalDateTime.now();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                simulationIteration(terrainController);
                synchronized (interval) {
                    if (--interval[0] <= 1) {
                        timer.cancel();
                        synchronized (weatherConditionsList) {
                            String resFilename = collectResultsToFiles(DateUtils.calculateInSeconds(iterationStart, LocalDateTime.now()),
                                weatherConditionsList);
                            Platform.runLater(() -> terrainController.showResultsScreen(resFilename));
                        }
                    }
                }
            }
        }, 1000, taskPeriod);
        if (algorithmType == AlgorithmType.FIXED_TIME) {
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
        //change weather dynamically
        if (dynamicWeather) {
            Timer weatherTimer = new Timer(true);
            weatherTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    synchronized (weatherConditions) {
                        weatherConditions = WeatherEnum.getRandom();
                        synchronized (weatherConditionsList) {
                            weatherConditionsList.add(weatherConditions);
                        }
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

    private synchronized String collectResultsToFiles(long simulationTimeInSeconds, List<WeatherEnum> weatherConditionsList) {
        return ExcelUtils.exportResults(getAllBelts(), weatherConditionsList, simulationTimeInSeconds);
    }

    private void simulationIteration(final TerrainController terrainController) {
        Platform.runLater(() -> {
            boolean changeLightsSwitch = false;
            for (TrafficBelt belt : SimulationController.this.getAllBelts()) {
                terrainController.removeCarsFromStage(belt.moveCars());
                ImageView res = belt.addCar(weatherConditions);
                if (res != null) {
                    terrainController.addCarOnStage(res);
                }
                if (belt.isAboveMaxTries() && algorithmType == AlgorithmType.CARS_COUNT) {
                    changeLightsSwitch = true;
                }
            }
            if (changeLightsSwitch) {
                SimulationController.this.getAllBelts().forEach(TrafficBelt::resetCarsTriesCounter);
                crossings.forEach(TrafficLightsAndCrossing::changeLights);
            }
        });
    }

    private List<TrafficLightsAndCrossing> initCrossings(int verticalBeltsCount, int verticalBelts2Count,
                                                         int horizontalBeltsCount, int windowHeight, int windowWidth) {
        List<TrafficLightsAndCrossing> res = new ArrayList<>();
        int yFrom = windowHeight / 2 - horizontalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;
        int yTo = windowHeight / 2 + horizontalBeltsCount * TrafficBelt.BELT_HEIGHT + 3;

        int x1From = windowWidth / 4 - verticalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;
        int x1To = windowWidth / 4 + verticalBeltsCount * TrafficBelt.BELT_HEIGHT + 3;

        int x2From = 3 * windowWidth / 4 - verticalBelts2Count * TrafficBelt.BELT_HEIGHT - 3;
        int x2To = 3 * windowWidth / 4 + verticalBelts2Count * TrafficBelt.BELT_HEIGHT + 3;

        res.add(new TrafficLightsAndCrossing(x1From, x1To, yFrom, yTo, verticalBeltsCount));
        res.add(new TrafficLightsAndCrossing(x2From, x2To, yFrom, yTo, verticalBelts2Count));
        return res;
    }

    private List<TrafficBelt> initVerticalBelts(int verticalBeltsCount, int windowHeight, int windowWidth, int carsLimit, int offset) {
        List<TrafficBelt> res = new ArrayList<>();
        int beltsXStart = offset - verticalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;   //we want to separate them
        int beltNr = 1;
        for (int i = 0; i < verticalBeltsCount; i++) {
            TrafficBelt belt =
                new DownTrafficBelt(beltNr++, carsLimit, beltsXStart, 0, TrafficBelt.BELT_HEIGHT, windowHeight + 20, null, null);
            res.add(belt);
            beltsXStart += TrafficBelt.BELT_HEIGHT;
        }
        beltsXStart += 6;
        beltNr = 1;
        for (int i = 0; i < verticalBeltsCount; i++) {
            TrafficBelt belt =
                new UpTrafficBelt(beltNr++, carsLimit, beltsXStart, 0, TrafficBelt.BELT_HEIGHT, windowHeight + 20, null, null);
            res.add(belt);
            beltsXStart += TrafficBelt.BELT_HEIGHT;
        }
        return res;
    }

    public List<TrafficBelt> initHorizontalBelts(int horizontalBeltsCount, int windowHeight, int windowWidth, int carsLimit,
                                                 int verticalBeltsCount, int verticalBelts2Count) {
        List<TrafficBelt> res = new ArrayList<>();
        speedRadars = new ArrayList<>();
        int leftBeltSpeedControlStart = (int)(3.0 * windowWidth / 4.0 - verticalBeltsCount * (TrafficBelt.BELT_HEIGHT + 3.0)) - 10;
        int leftBeltSpeedControlEnd = (int)(leftBeltSpeedControlStart - windowWidth / 6.0);

        int rightBeltSpeedControlStart = (int) (windowWidth / 4.0 + verticalBelts2Count * (TrafficBelt.BELT_HEIGHT + 3.0)) + 10;
        int rightBeltSpeedControlEnd = (int) (rightBeltSpeedControlStart + windowWidth / 6.0);
        int beltsYStart = windowHeight / 2 - horizontalBeltsCount * TrafficBelt.BELT_HEIGHT - 3;   //we want to separate them

        speedRadars.addAll(Arrays.asList(new SpeedRadar(leftBeltSpeedControlStart - 10, beltsYStart - 40)));
        speedRadars.addAll(Arrays.asList(new SpeedRadar(leftBeltSpeedControlEnd - 10, beltsYStart - 40)));
        int beltNr = 1;
        for (int i = 0; i < horizontalBeltsCount; i++) {
            TrafficBelt belt = new LeftTrafficBelt(beltNr++, carsLimit, 0, beltsYStart, windowWidth + 20, TrafficBelt.BELT_HEIGHT,
                leftBeltSpeedControlStart, leftBeltSpeedControlEnd);
            res.add(belt);
            beltsYStart += TrafficBelt.BELT_HEIGHT;
        }
        beltsYStart += 6;
        beltNr = 1;
        for (int i = 0; i < horizontalBeltsCount; i++) {
            TrafficBelt belt = new RightTrafficBelt(beltNr++, carsLimit, 0, beltsYStart, windowWidth + 20, TrafficBelt.BELT_HEIGHT,
                rightBeltSpeedControlStart, rightBeltSpeedControlEnd);
            res.add(belt);
            beltsYStart += TrafficBelt.BELT_HEIGHT;
        }

        speedRadars.addAll(Arrays.asList(new SpeedRadar(rightBeltSpeedControlStart + 10, beltsYStart + 10)));
        speedRadars.addAll(Arrays.asList(new SpeedRadar(rightBeltSpeedControlEnd + 10, beltsYStart + 10)));
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

    public WeatherEnum getWeatherConditions() {
        return weatherConditions;
    }

    public List<SpeedRadar> getSpeedRadars() {
        return speedRadars;
    }
}
