package main.utils;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.model.belts.TrafficBelt;
import main.model.enums.WeatherEnum;
import main.model.results.SpeedResult;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

public class ExcelUtils {
    public static String exportResults(List<TrafficBelt> allBelts, List<WeatherEnum> weatherConditions, long simulationDuration) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String filename = "speeds_" + formatter.format(LocalDateTime.now()) + ".xls";
            FileOutputStream fos = new FileOutputStream(filename);
            HSSFWorkbook workbook = new HSSFWorkbook();
            exportCarsSimulationTimeAndWeather(allBelts, weatherConditions, simulationDuration, workbook);
            exportCarsThatLeftDuringTheWeather(allBelts, workbook);
            exportSpeeds(allBelts, workbook);
            exportSpeedMeasurements(allBelts, workbook);
            exportOverSpeedMeasurements(allBelts, workbook);
            exportSpeedDuringTheWeather(allBelts, workbook);
            workbook.write(fos);
            fos.flush();
            fos.close();
            return filename;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void exportCarsSimulationTimeAndWeather(List<TrafficBelt> allBelts, List<WeatherEnum> weatherConditions,
                                                           long simulationDuration, HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.createSheet("SimulationSummary");
        int rowNum = 0;
        int cellNum = 0;
        HSSFRow row = sheet.createRow(rowNum++);
        row.createCell(cellNum++).setCellValue("Simulation time");
        row.createCell(cellNum++).setCellValue(simulationDuration + "s.");
        cellNum = 0;
        row = sheet.createRow(rowNum++);
        row.createCell(cellNum++).setCellValue("All cars that left the stage");
        long allCarsCount = 0;
        for (TrafficBelt belt : allBelts) {
            allCarsCount += belt.getCarsThatLeftTheStage();
        }
        row.createCell(cellNum++).setCellValue(allCarsCount);
        cellNum = 0;
        row = sheet.createRow(rowNum++);
        row.createCell(cellNum++).setCellValue("Weather conditions");
        for (WeatherEnum weatherCondition : weatherConditions) {
            row.createCell(cellNum++).setCellValue(weatherCondition.toString());
        }
        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Cars that left the stage on belts");
        ++rowNum;
        row = sheet.createRow(rowNum++);
        for (TrafficBelt belt : allBelts) {
            cellNum = 0;
            row = sheet.createRow(rowNum++);
            row.createCell(cellNum++).setCellValue(belt.getBeltDirection().toString() + " " + belt.getBeltNumber());
            row.createCell(cellNum++).setCellValue(belt.getCarsThatLeftTheStage());
        }
    }

    private static void exportCarsThatLeftDuringTheWeather(List<TrafficBelt> allBelts, HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.createSheet("CarsLeftTheStageDuringWeather");
        int rowNum = 0;
        Map<WeatherEnum, Long> allResultsWithoutBeltsDivision = new HashMap<>();
        for (TrafficBelt belt : allBelts) {
            HSSFRow row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(belt.getBeltDirection().toString() + " " + belt.getBeltNumber());
            for (Map.Entry<WeatherEnum, Long> weatherEnumLongEntry : belt.getCarsThatLeftTheStageWithWeather().entrySet()) {
                HSSFRow entryRow = sheet.createRow(rowNum++);
                int cellNo = 0;
                entryRow.createCell(cellNo++).setCellValue(weatherEnumLongEntry.getKey().toString());
                entryRow.createCell(cellNo++).setCellValue(weatherEnumLongEntry.getValue());

                if (!allResultsWithoutBeltsDivision.containsKey(weatherEnumLongEntry.getKey())) {
                    allResultsWithoutBeltsDivision.put(weatherEnumLongEntry.getKey(), 0L);
                }
                allResultsWithoutBeltsDivision.put(weatherEnumLongEntry.getKey(),
                    allResultsWithoutBeltsDivision.get(weatherEnumLongEntry.getKey()) + weatherEnumLongEntry.getValue());
            }
        }
        rowNum+=2;
        HSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Results without belts division:");
        for (Map.Entry<WeatherEnum, Long> weatherEnumLongEntry : allResultsWithoutBeltsDivision.entrySet()) {
            HSSFRow entryRow = sheet.createRow(rowNum++);
            int cellNo = 0;
            entryRow.createCell(cellNo++).setCellValue(weatherEnumLongEntry.getKey().toString());
            entryRow.createCell(cellNo++).setCellValue(weatherEnumLongEntry.getValue());
        }
    }

    private static void exportSpeedDuringTheWeather(List<TrafficBelt> allBelts, HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.createSheet("SpeedForWeather");
        int rowNum = 0;
        for (TrafficBelt belt : allBelts) {
            HSSFRow row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(belt.getBeltDirection().toString() + " " + belt.getBeltNumber());
            for (SpeedResult speedResult : belt.getSpeedResults()) {
                for (WeatherEnum weather : speedResult.getWeatherSpeeds().keySet()) {
                    if (speedResult.getWeatherSpeeds().get(weather).size() == 1 && speedResult.getWeatherSpeeds().get(weather).get(0) == 0L) {
                        continue;
                    }
                    HSSFRow speedRow = sheet.createRow(rowNum++);
                    int cellNo = 0;
                    Cell weatherNameCell = speedRow.createCell(cellNo++);
                    weatherNameCell.setCellValue(weather.toString());
                    for (Long speed : speedResult.getWeatherSpeeds().get(weather)) {
                        if (speed == 0) {
                            continue;
                        }
                        speedRow.createCell(cellNo++).setCellValue(speed);
                    }
                }
            }
        }
    }

    private static void exportOverSpeedMeasurements(List<TrafficBelt> allBelts, HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.createSheet("OverSpeedMeasurements");
        int rowNum = 0;
        for (TrafficBelt belt : allBelts) {
            HSSFRow row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(belt.getBeltDirection().toString() + " " + belt.getBeltNumber());
            for (SpeedResult speedResult : belt.getSpeedResults()) {
                if (NumberUtils.isInteger(speedResult.getRadarSpeed()) && Integer.parseInt(speedResult.getRadarSpeed()) > 120) {
                    HSSFRow speedRow = sheet.createRow(rowNum++);
                    Cell radarSpeedCell = speedRow.createCell(0);
                    Cell radarSpeedValCell = speedRow.createCell(1);
                    radarSpeedCell.setCellValue("RadarMeasuredOverSpeed:");
                    radarSpeedValCell.setCellValue(Integer.parseInt(speedResult.getRadarSpeed()));
                }
            }
        }
    }

    private static void exportSpeedMeasurements(List<TrafficBelt> allBelts, HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.createSheet("SpeedMeasurements");
        int rowNum = 0;

        for (TrafficBelt belt : allBelts) {
            HSSFRow row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(belt.getBeltDirection().toString() + " " + belt.getBeltNumber());
            for (SpeedResult speedResult : belt.getSpeedResults()) {
                HSSFRow speedRow = sheet.createRow(rowNum++);
                Cell radarSpeedCell = speedRow.createCell(0);
                Cell radarSpeedValCell = speedRow.createCell(1);
                radarSpeedCell.setCellValue("RadarMeasuredSpeed:");
                if (NumberUtils.isInteger(speedResult.getRadarSpeed())) {
                    radarSpeedValCell.setCellValue(Integer.parseInt(speedResult.getRadarSpeed()));
                } else {
                    radarSpeedValCell.setCellValue(speedResult.getRadarSpeed());
                }
            }
        }
    }

    private static void exportSpeeds(List<TrafficBelt> allBelts, HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.createSheet("AverageSpeedResults");
        int rowNum = 0;

        for (TrafficBelt belt : allBelts) {
            HSSFRow row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(belt.getBeltDirection().toString() + " " + belt.getBeltNumber());
            for (SpeedResult speedResult : belt.getSpeedResults()) {
                HSSFRow speedRow = sheet.createRow(rowNum++);
                Cell avgSpeedCell = speedRow.createCell(0);
                Cell avgSpeedValCell = speedRow.createCell(1);
                avgSpeedCell.setCellValue("AverageSpeed:");
                avgSpeedValCell.setCellValue(speedResult.getAverageSpeed());
            }
        }
    }
}
