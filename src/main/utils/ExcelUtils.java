package main.utils;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import main.model.TrafficBelt;
import main.model.results.SpeedResult;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

public class ExcelUtils {
    public static void exportSpeeds(List<TrafficBelt> allBelts) {
        try {
            FileOutputStream fos =  new FileOutputStream("speeds_" + LocalDate.now().toString() + ".xls");
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("SpeedResults");
            int rowNum = 0;

            for (TrafficBelt belt : allBelts) {
                HSSFRow row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                cell.setCellValue(belt.getBeltDirection().toString());
                for (SpeedResult speedResult : belt.getSpeedResults()) {
                    HSSFRow speedRow = sheet.createRow(rowNum++);
                    Cell avgSpeedCell = speedRow.createCell(0);
                    Cell avgSpeedValCell = speedRow.createCell(1);
                    avgSpeedCell.setCellValue("AverageSpeed:");
                    avgSpeedValCell.setCellValue(speedResult.getAverageSpeed());
                    Cell maxSpeedCell = speedRow.createCell(2);
                    Cell maxSpeedValCell = speedRow.createCell(3);
                    maxSpeedCell.setCellValue("MaxSpeedReached:");
                    maxSpeedValCell.setCellValue(speedResult.getMaxSpeedReached());
                }
            }
            workbook.write(fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
