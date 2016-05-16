package main.controllers;

import main.model.TrafficBelt;
import main.model.enums.DirectionEnum;

import java.util.ArrayList;
import java.util.List;

public class SimulationController {
    private List<TrafficBelt> verticalBelts;
    private List<TrafficBelt> verticalBelts2;
    private List<TrafficBelt> horizontalBelts;

    public SimulationController(int verticalBeltsCount, int verticalBelts2Count, int horizontalBeltsCount,
                                int carsLimit, int width, int height) {
        verticalBelts = initVerticalBelts(verticalBeltsCount, height, width, carsLimit, width / 4);
        verticalBelts2 = initVerticalBelts(verticalBelts2Count, height, width, carsLimit, 3 * width / 4);
        horizontalBelts = initHorizontalBelts(horizontalBeltsCount, height, width, carsLimit);
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
}
