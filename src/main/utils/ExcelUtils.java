package main.utils;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import main.model.TrafficBelt;
import main.model.results.SpeedResult;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

public class ExcelUtils {
    public static String exportResults(List<TrafficBelt> allBelts) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String filename = "speeds_" + formatter.format(LocalDateTime.now()) + ".xls";
            FileOutputStream fos =  new FileOutputStream(filename);
            HSSFWorkbook workbook = new HSSFWorkbook();
            exportSpeeds(allBelts, workbook);
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

                Cell radarSpeedCell = speedRow.createCell(2);
                Cell radarSpeedValCell = speedRow.createCell(3);
                radarSpeedCell.setCellValue("RadarMeasuredSpeed:");
                if (NumberUtils.isInteger(speedResult.getRadarSpeed())) {
                    radarSpeedValCell.setCellValue(Integer.parseInt(speedResult.getRadarSpeed()));
                } else {
                    radarSpeedValCell.setCellValue(speedResult.getRadarSpeed());
                }
            }
        }
    }
}
