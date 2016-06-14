package main.utils;


import main.model.belts.TrafficBelt;
import main.model.enums.WeatherEnum;
import main.model.results.SpeedResult;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by Krzysztof Baran
 * Narzędzia służące do eksportu wyników symulacji do pliku excel
 */
public class ExcelUtils {
    /**
     * DateTimeFormatter do formatowania daty w nazwach plików
     */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Funkcja główna, której zadaniem jest utwożenie nowego dokumentu typu excel oraz workbooka.
     * Dodatkowo woła funkcje pomocnicze do eksportu poszczególnych zakładek dokumentu.
     *
     * @param allBelts           Wszystkie pasy drogowe, z których możemy zczytać wyniki symulacji
     * @param weatherConditions  Lista warunków pogodowych jakie panowały na drodze podczas symulacji
     * @param simulationDuration Czas trwania symulacji w sekundach
     * @return Nazwa plików z podsumowaniem, który został utworzony przez aplikację
     */
    public static List<String> exportResults(List<TrafficBelt> allBelts, List<WeatherEnum> weatherConditions, long simulationDuration) {
        try {
            InputStream is = ExcelUtils.class.getClassLoader().getResourceAsStream("resources/charts_template.xlsx");
            HSSFWorkbook dataWorkbook = new HSSFWorkbook();
            XSSFWorkbook chartsWorkbook = new XSSFWorkbook(OPCPackage.open(is));
            exportCarsSimulationTimeAndWeather(allBelts, weatherConditions, simulationDuration, dataWorkbook);
            exportCarsThatLeftDuringTheWeather(allBelts, dataWorkbook, chartsWorkbook);
            exportCollisions(allBelts, dataWorkbook, chartsWorkbook);
            exportSpeeds(allBelts, dataWorkbook);
            exportSpeedMeasurements(allBelts, dataWorkbook);
            exportOverSpeedMeasurements(allBelts, dataWorkbook);
            exportSpeedDuringTheWeather(allBelts, dataWorkbook);
            exportSpeedChartsWithinTheSpeed(allBelts, chartsWorkbook);
            exportRadarSpeedChartsWithinTheSpeed(allBelts, chartsWorkbook);
            exportOverSpeedChartsWithinTheSpeed(allBelts, chartsWorkbook);
            exportAverageSpeedChartsWithinTheWeather(allBelts, chartsWorkbook);
            String filename = "speeds_" + formatter.format(LocalDateTime.now()) + ".xls";
            String chartsFilename = "charts_" + formatter.format(LocalDateTime.now()) + ".xlsx";
            FileOutputStream fos = new FileOutputStream(filename);
            FileOutputStream chartsFos = new FileOutputStream(chartsFilename);
            dataWorkbook.write(fos);
            chartsWorkbook.write(chartsFos);
            fos.flush();
            fos.close();
            List<String> files = new ArrayList<>(Arrays.asList(filename, chartsFilename));
            return files;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Metoda eksportująca średnie prędkości względem pogody przeliczone odpowiednio
     *
     * @param allBelts       Wszystkie pasy drogowe
     * @param chartsWorkbook Workbook z wykresami
     */
    private static void exportAverageSpeedChartsWithinTheWeather(List<TrafficBelt> allBelts, XSSFWorkbook chartsWorkbook) {
        XSSFSheet sheet = chartsWorkbook.getSheet("Weather average speed");
        int rowNum = 0;
        XSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Średnia prędkość samochodów dla zadanej pogody");
        Map<WeatherEnum, List<Long>> weatherSpeeds = new HashMap<>();
        for (TrafficBelt belt : allBelts) {
            for (SpeedResult result : belt.getSpeedResults()) {
                for (Map.Entry<WeatherEnum, List<Long>> weatherEntry : result.getWeatherSpeeds().entrySet()) {
                    if (!weatherSpeeds.containsKey(weatherEntry.getKey())) {
                        weatherSpeeds.put(weatherEntry.getKey(), new ArrayList<>());
                    }
                    weatherSpeeds.get(weatherEntry.getKey()).addAll(weatherEntry.getValue());
                }
            }
        }
        for (Map.Entry<WeatherEnum, List<Long>> weatherEntry : weatherSpeeds.entrySet()) {
            double avgSpeed = 0.0;
            for (Long speed : weatherEntry.getValue()) {
                avgSpeed += speed;
            }
            if (avgSpeed != 0) {
                avgSpeed = avgSpeed / (double) weatherEntry.getValue().size();
            }
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(weatherEntry.getKey().getPlName());
            row.createCell(1).setCellValue((int) avgSpeed);
        }
    }

    /**
     * Funkcja służąca do eksportu pogrupowanych danych dla samochodów, które przekroczyły prędkość na pomiarze odcinkowym
     *
     * @param allBelts       Wszystkie pasy drogowe
     * @param chartsWorkbook Workbook z wykresami
     */
    private static void exportOverSpeedChartsWithinTheSpeed(List<TrafficBelt> allBelts, XSSFWorkbook chartsWorkbook) {
        XSSFSheet sheet = chartsWorkbook.getSheet("Cars radar over speeds");
        int rowNum = 0;
        XSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Prędkości samochodów, które przekroczyły prędkość podczas pomiaru odcinkowego");
        Map<String, Long> carsWithinTheSpeed = new TreeMap<>((Comparator<String>) (o1, o2) ->
                Integer.valueOf(o1.split(" ")[0]).compareTo(Integer.valueOf(o2.split(" ")[0]))
        );
        long minSpeedRange = Long.MAX_VALUE, maxSpeedRange = 120L;

        for (TrafficBelt belt : allBelts) {
            for (SpeedResult result : belt.getSpeedResults()) {
                if (!NumberUtils.isInteger(result.getRadarSpeed())) {
                    continue;
                }
                if (Long.valueOf(result.getRadarSpeed()) > 120L && Long.valueOf(result.getRadarSpeed()) < 300L) {
                    long speed = Long.valueOf(result.getRadarSpeed());
                    long lowerBound = speed / 10 * 10;
                    long higherBound = lowerBound + 9;
                    if (lowerBound < minSpeedRange) {
                        minSpeedRange = lowerBound;
                    }
                    if (higherBound > maxSpeedRange) {
                        maxSpeedRange = higherBound;
                    }
                }
            }
        }
        //inicjalizujemy wejściowe przedziały, żeby były wszystkie, nawet puste
        for (long i = minSpeedRange; i <= maxSpeedRange; i += 10) {
            carsWithinTheSpeed.put(i + " - " + (i + 9), 0L);
        }

        for (TrafficBelt belt : allBelts) {
            for (SpeedResult result : belt.getSpeedResults()) {
                if (!NumberUtils.isInteger(result.getRadarSpeed())) {
                    continue;
                }
                if (Long.valueOf(result.getRadarSpeed()) > 120L && Long.valueOf(result.getRadarSpeed()) < 300L) {
                    long speed = Long.valueOf(result.getRadarSpeed());
                    long lowerBound = speed / 10 * 10;
                    long higherBound = lowerBound + 9;
                    String range = lowerBound + " - " + higherBound;
                    if (!carsWithinTheSpeed.containsKey(range)) {
                        carsWithinTheSpeed.put(range, 0L);
                    }
                    carsWithinTheSpeed.put(range, carsWithinTheSpeed.get(range) + 1);
                }
            }
        }

        for (Map.Entry<String, Long> speedRange : carsWithinTheSpeed.entrySet()) {
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(speedRange.getKey());
            row.createCell(1).setCellValue(speedRange.getValue());
        }
    }

    /**
     * Funkcja służąca do eksportu pogrupowanych danych dla samochodów, które brały udział w pomiarze odcinkowym
     *
     * @param allBelts       Wszystkie pasy drogowe
     * @param chartsWorkbook Workbook z wykresami
     */
    private static void exportRadarSpeedChartsWithinTheSpeed(List<TrafficBelt> allBelts, XSSFWorkbook chartsWorkbook) {
        XSSFSheet sheet = chartsWorkbook.getSheet("Cars radar speeds");
        int rowNum = 0;
        XSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Prędkości samochodów podczas pomiaru odcinkowego");
        Map<String, Long> carsWithinTheSpeed = new TreeMap<>((Comparator<String>) (o1, o2) ->
                Integer.valueOf(o1.split(" ")[0]).compareTo(Integer.valueOf(o2.split(" ")[0]))
        );
        long minSpeedRange = Long.MAX_VALUE, maxSpeedRange = 0L;

        for (TrafficBelt belt : allBelts) {
            for (SpeedResult result : belt.getSpeedResults()) {
                if (!NumberUtils.isInteger(result.getRadarSpeed()) || Long.valueOf(result.getRadarSpeed()) <= 0L ||
                        Long.valueOf(result.getRadarSpeed()) > 300L) {
                    continue;
                }
                long speed = Long.valueOf(result.getRadarSpeed());
                long lowerBound = speed / 10;
                if (lowerBound % 2 == 1) {
                    lowerBound--;
                }
                lowerBound *= 10;
                long higherBound = lowerBound + 19;
                if (lowerBound < minSpeedRange) {
                    minSpeedRange = lowerBound;
                }
                if (higherBound > maxSpeedRange) {
                    maxSpeedRange = higherBound;
                }
            }
        }
        //inicjalizujemy wejściowe przedziały, żeby były wszystkie, nawet puste
        for (long i = minSpeedRange; i <= maxSpeedRange; i += 20) {
            carsWithinTheSpeed.put(i + " - " + (i + 19), 0L);
        }

        for (TrafficBelt belt : allBelts) {
            for (SpeedResult result : belt.getSpeedResults()) {
                if (!NumberUtils.isInteger(result.getRadarSpeed()) || Long.valueOf(result.getRadarSpeed()) <= 0L ||
                        Long.valueOf(result.getRadarSpeed()) > 300L) {
                    continue;
                }
                long speed = Long.valueOf(result.getRadarSpeed());
                long lowerBound = speed / 10;
                if (lowerBound % 2 == 1) {
                    lowerBound--;
                }
                lowerBound *= 10;
                long higherBound = lowerBound + 19;
                String range = lowerBound + " - " + higherBound;
                if (!carsWithinTheSpeed.containsKey(range)) {
                    carsWithinTheSpeed.put(range, 0L);
                }
                carsWithinTheSpeed.put(range, carsWithinTheSpeed.get(range) + 1);
            }
        }

        for (Map.Entry<String, Long> speedRange : carsWithinTheSpeed.entrySet()) {
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(speedRange.getKey());
            row.createCell(1).setCellValue(speedRange.getValue());
        }
    }

    /**
     * Metoda eksportująca średnią prędkość samochodów z podziałem na sekcje co 20km/h
     *
     * @param allBelts       Wszystkie pasy drogowe
     * @param chartsWorkbook Workbook z generowanymi wykresami
     */
    private static void exportSpeedChartsWithinTheSpeed(List<TrafficBelt> allBelts, XSSFWorkbook chartsWorkbook) {
        XSSFSheet sheet = chartsWorkbook.getSheet("Cars avg speeds");
        int rowNum = 0;
        XSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Średnie prędkości samochodów");
        Map<String, Long> carsWithinTheSpeed = new TreeMap<>((Comparator<String>) (o1, o2) ->
                Integer.valueOf(o1.split(" ")[0]).compareTo(Integer.valueOf(o2.split(" ")[0]))
        );
        long minSpeedRange = Long.MAX_VALUE, maxSpeedRange = 0L;

        for (TrafficBelt belt : allBelts) {
            for (SpeedResult result : belt.getSpeedResults()) {
                long avgSpeed = result.getAverageSpeed();
                long lowerBound = avgSpeed / 10;
                if (lowerBound % 2 == 1) {
                    lowerBound--;
                }
                lowerBound *= 10;
                long higherBound = lowerBound + 19;
                if (lowerBound < minSpeedRange) {
                    minSpeedRange = lowerBound;
                }
                if (higherBound > maxSpeedRange) {
                    maxSpeedRange = higherBound;
                }
            }
        }
        //inicjalizujemy wejściowe przedziały, żeby były wszystkie, nawet puste
        for (long i = minSpeedRange; i <= maxSpeedRange; i += 20) {
            carsWithinTheSpeed.put(i + " - " + (i + 19), 0L);
        }

        for (TrafficBelt belt : allBelts) {
            for (SpeedResult result : belt.getSpeedResults()) {
                long avgSpeed = result.getAverageSpeed();
                long lowerBound = avgSpeed / 10;
                if (lowerBound % 2 == 1) {
                    lowerBound--;
                }
                lowerBound *= 10;
                long higherBound = lowerBound + 19;
                String range = lowerBound + " - " + higherBound;
                if (!carsWithinTheSpeed.containsKey(range)) {
                    carsWithinTheSpeed.put(range, 0L);
                }
                carsWithinTheSpeed.put(range, carsWithinTheSpeed.get(range) + 1);
            }
        }

        for (Map.Entry<String, Long> speedRange : carsWithinTheSpeed.entrySet()) {
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(speedRange.getKey());
            row.createCell(1).setCellValue(speedRange.getValue());
        }
    }

    /**
     * Funkcja tworząca arkusz 'SimulationSummary'.
     * Zapisuje kolejno:
     * 1. Czas trwania symulacji
     * 2. Ilość samochodów, które opuściły ekran symulacji (zliczane z wszystkich pasów drogowych)
     * 3. Kolejno panujące warunki pogodowe na drodze
     * 4. Wyszczególnienie ilości saochodów, które opuściły scenę symulacji dla każdego z pasów drogowych
     *
     * @param allBelts           Lista wszystkich pasów drogowych, z których zczytujemy wyniki
     * @param weatherConditions  Lista warunków pogodowych jakie panowały na drodze podczas symulacji
     * @param simulationDuration Czas trwania symulacji w sekundach
     * @param dataWorkbook       Workbook excelowy, do którego zapisujemy arkusz
     */
    private static void exportCarsSimulationTimeAndWeather(List<TrafficBelt> allBelts, List<WeatherEnum> weatherConditions,
                                                           long simulationDuration, HSSFWorkbook dataWorkbook) {
        HSSFSheet sheet = dataWorkbook.createSheet("SimulationSummary");
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
        sheet.createRow(rowNum++);
        for (TrafficBelt belt : allBelts) {
            cellNum = 0;
            row = sheet.createRow(rowNum++);
            row.createCell(cellNum++).setCellValue(belt.getBeltDirection().toString() + " " + belt.getBeltNumber());
            row.createCell(cellNum++).setCellValue(belt.getCarsThatLeftTheStage());
        }
    }

    /**
     * Funkcja, której zadaniem jest utworzenie arkusza 'CarsLeftTheStageDuringWeather'
     * Zbiera wyniki z wszystkich pasów, a następnie pobiera dane o ilości samochodów, które opuściły każdy z pasów
     * w danych warunkach pogodowych. Następnie zapisywane jest podsumowanie,
     * w których nie wyszczególniamy pasów drogowych.
     *
     * @param allBelts     Wszystkie pasy drogowe, z których możemy zczytać wyniki symulacji
     * @param dataWorkbook Workbook excelowy, do którego zapisujemy arkusz
     */
    private static void exportCarsThatLeftDuringTheWeather(List<TrafficBelt> allBelts, HSSFWorkbook dataWorkbook, XSSFWorkbook chartsWorkbook) {
        HSSFSheet sheet = dataWorkbook.createSheet("CarsLeftTheStageDuringWeather");
        XSSFSheet chartSheet = chartsWorkbook.getSheet("CarsLeftTheStageDuringWeather");
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
        rowNum += 2;
        HSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Results without belts division:");
        int charRowNum = 0;
        chartSheet.createRow(charRowNum++).createCell(0).setCellValue("Samochody, które zakończyły symulację względem pogody");
        for (Map.Entry<WeatherEnum, Long> weatherEnumLongEntry : allResultsWithoutBeltsDivision.entrySet()) {
            XSSFRow charEntryRow = chartSheet.createRow(charRowNum++);
            HSSFRow entryRow = sheet.createRow(rowNum++);
            int cellNo = 0;
            charEntryRow.createCell(cellNo).setCellValue(weatherEnumLongEntry.getKey().getPlName());
            entryRow.createCell(cellNo++).setCellValue(weatherEnumLongEntry.getKey().toString());
            charEntryRow.createCell(cellNo).setCellValue(weatherEnumLongEntry.getValue());
            entryRow.createCell(cellNo++).setCellValue(weatherEnumLongEntry.getValue());
        }
    }

    /**
     * Funkcja tworząca arkusz 'Collisions'.
     * Zapisuje kolejno:
     * Funkcja ta dla każdego z pasów i dla każdego z samochodów, zlicza w jakiej pogodzie ile było wypadków na danym pasie i zapisuje te wartości
     * Potem zapisywane jest podsumowanie względem pogody, bez pasów
     *
     * @param allBelts     Lista wszystkich pasów drogowych, z których zczytujemy wyniki
     * @param dataWorkbook Workbook excelowy, do którego zapisujemy arkusz
     */
    private static void exportCollisions(List<TrafficBelt> allBelts, HSSFWorkbook dataWorkbook, XSSFWorkbook chartsWorkbook) {
        HSSFSheet sheet = dataWorkbook.createSheet("Collisions");
        XSSFSheet chartsSheet = chartsWorkbook.getSheet("Collisions");
        int rowNum = 0;
        Map<WeatherEnum, Integer> collisionsInWeather = new HashMap<>();
        for (TrafficBelt belt : allBelts) {
            HSSFRow row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(belt.getBeltDirection().toString() + " " + belt.getBeltNumber());
            Map<WeatherEnum, Integer> collisionsInWeatherForBelt = new HashMap<>();
            for (SpeedResult speedResult : belt.getSpeedResults()) {
                if (speedResult.isHadCollision()) {
                    if (!collisionsInWeather.containsKey(speedResult.getCollisionWeather())) {
                        collisionsInWeather.put(speedResult.getCollisionWeather(), 0);
                    }
                    if (!collisionsInWeatherForBelt.containsKey(speedResult.getCollisionWeather())) {
                        collisionsInWeatherForBelt.put(speedResult.getCollisionWeather(), 0);
                    }
                    collisionsInWeatherForBelt
                            .put(speedResult.getCollisionWeather(), collisionsInWeatherForBelt.get(speedResult.getCollisionWeather()) + 1);
                    collisionsInWeather.put(speedResult.getCollisionWeather(), collisionsInWeather.get(speedResult.getCollisionWeather()) + 1);
                }
            }
            if (collisionsInWeatherForBelt.isEmpty()) {
                sheet.createRow(rowNum++).createCell(0).setCellValue("No collisions detected");
            } else {
                for (Map.Entry<WeatherEnum, Integer> weatherBeltCollisionEntry : collisionsInWeatherForBelt.entrySet()) {
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(weatherBeltCollisionEntry.getKey().name());
                    row.createCell(1).setCellValue(weatherBeltCollisionEntry.getValue());
                }
            }
        }
        rowNum += 2;
        int chartsRowNum = 0;
        sheet.createRow(rowNum++).createCell(0).setCellValue("Summary");
        chartsSheet.createRow(chartsRowNum++).createCell(0).setCellValue("Liczba samochodów biorących udział w kolizji względem pogody");
        if (collisionsInWeather.isEmpty()) {
            sheet.createRow(rowNum++).createCell(0).setCellValue("No collisions detected");
            XSSFRow chartsRow = chartsSheet.createRow(chartsRowNum++);
            chartsRow.createCell(0).setCellValue("Brak kolizji");
            chartsRow.createCell(1).setCellValue(0);
        } else {
            for (Map.Entry<WeatherEnum, Integer> weatherBeltCollisionEntry : collisionsInWeather.entrySet()) {
                HSSFRow row = sheet.createRow(rowNum++);
                XSSFRow chartsRow = chartsSheet.createRow(chartsRowNum++);
                row.createCell(0).setCellValue(weatherBeltCollisionEntry.getKey().name());
                row.createCell(1).setCellValue(weatherBeltCollisionEntry.getValue());
                chartsRow.createCell(0).setCellValue(weatherBeltCollisionEntry.getKey().getPlName());
                chartsRow.createCell(1).setCellValue(weatherBeltCollisionEntry.getValue());
            }
        }
    }

    /**
     * Funkcja, która tworzy arkusz 'SpeedForWeather'
     * Funkcja ta dla każdego z pasów, a następnie dla każdego z samochodów zapisuje pogodę oraz średnią prędkość
     * jaką posiadał samochód w zadanych warunkach pogodowych
     *
     * @param allBelts     Wszystkie pasy drogowe, z których możemy zczytać wyniki symulacji
     * @param dataWorkbook Workbook excelowy, do którego zapisujemy arkusz
     */
    private static void exportSpeedDuringTheWeather(List<TrafficBelt> allBelts, HSSFWorkbook dataWorkbook) {
        HSSFSheet sheet = dataWorkbook.createSheet("SpeedForWeather");
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

    /**
     * Funkcja tworząca arkusz 'OverSpeedMeasurements'
     * Funkcja zbiera z wszystkich samochodów odcinkowe pomiary prędkości na poszczególnych pasach (jeśli istnieje),
     * a następnie sprawdzamy, czy prędkość na danym odcinku przekracza dozwoloną prędkość na trasie (120 km/h).
     * Jeśli tak, to rejestruje zadany przypadek na arkuszu.
     *
     * @param allBelts     Wszystkie pasy drogowe, z których możemy zczytać wyniki symulacji
     * @param dataWorkbook Workbook excelowy, do którego zapisujemy arkusz
     */
    private static void exportOverSpeedMeasurements(List<TrafficBelt> allBelts, HSSFWorkbook dataWorkbook) {
        HSSFSheet sheet = dataWorkbook.createSheet("OverSpeedMeasurements");
        int rowNum = 0;
        for (TrafficBelt belt : allBelts) {
            HSSFRow row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(belt.getBeltDirection().toString() + " " + belt.getBeltNumber());
            for (SpeedResult speedResult : belt.getSpeedResults()) {
                if (NumberUtils.isInteger(speedResult.getRadarSpeed()) && Integer.parseInt(speedResult.getRadarSpeed()) > 120
                        && Integer.parseInt(speedResult.getRadarSpeed()) < 300) {
                    HSSFRow speedRow = sheet.createRow(rowNum++);
                    Cell radarSpeedCell = speedRow.createCell(0);
                    Cell radarSpeedValCell = speedRow.createCell(1);
                    radarSpeedCell.setCellValue("RadarMeasuredOverSpeed:");
                    radarSpeedValCell.setCellValue(Integer.parseInt(speedResult.getRadarSpeed()));
                }
            }
        }
    }

    /**
     * Funkcja tworząca arkusz 'SpeedMeasurements'
     * Zbiera ona dla wszystkich samochodów wartości pomiarów odcinkowych i zapisuje je na formularzu
     * (z podziałem na pasy)
     *
     * @param allBelts     Lista wszystkich pasów drogowych, z których zczytujemy wyniki
     * @param dataWorkbook Workbook excelowy, do którego zapisujemy arkusz
     */
    private static void exportSpeedMeasurements(List<TrafficBelt> allBelts, HSSFWorkbook dataWorkbook) {
        HSSFSheet sheet = dataWorkbook.createSheet("SpeedMeasurements");
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
                if (NumberUtils.isInteger(speedResult.getRadarSpeed()) && Integer.parseInt(speedResult.getRadarSpeed()) > 0
                        && Integer.parseInt(speedResult.getRadarSpeed()) < 300) {
                    radarSpeedValCell.setCellValue(Integer.parseInt(speedResult.getRadarSpeed()));
                } else {
                    radarSpeedValCell.setCellValue(speedResult.getRadarSpeed());
                }
            }
        }
    }

    /**
     * Funkcja tworząca arkusz 'AverageSpeedResults'
     * Zadaniem jej jest pobranie ze wszystkich samochodów średnich prędkości przejazdów przez całą przebytą trasę.
     * Prędkości te rejestrowane są z podziałem na pasy drogowe
     *
     * @param allBelts     Lista wszystkich pasów drogowych, z których zczytujemy wyniki
     * @param dataWorkbook Workbook excelowy, do którego zapisujemy arkusz
     */
    private static void exportSpeeds(List<TrafficBelt> allBelts, HSSFWorkbook dataWorkbook) {
        HSSFSheet sheet = dataWorkbook.createSheet("AverageSpeedResults");
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
