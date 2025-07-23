package utils;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static utils.Utils.getDate;


public class PreviousResultsParser {
    private static class ResultInstance {
        private final String instanceName;
        private double ofValue;
        private double time;
        private double ttb;

        public ResultInstance(String instanceName, double ofValue, double time, double ttb) {
            this.instanceName = instanceName;
            this.ofValue = ofValue;
            this.time = time;
            this.ttb = ttb;
        }

        public String getInstanceName() {
            return instanceName;
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet s = wb.createSheet("DATA");

        String date = getDate();
        String outDir = "experiments/" + date;
        String outputFile = outDir + "/previous_results_final_set_paper_params_moretime.xlsx";
        // ROW FOR HEADINGS
        XSSFRow row = s.createRow(0);
        XSSFCell cell;
        int columnIdx = 0;
        int rowIdx = 0;
        cell = row.createCell(columnIdx);
        cell.setCellValue("Instance");
        columnIdx++;
        for (int i = 0; i < 10; i++) {
            for (String info : new String[]{"O.F Value iter " + i, "Time (s) iter " + i, "TTB (s) iter " + i}) {
                cell = row.createCell(columnIdx);
                cell.setCellValue(info);
                columnIdx++;
            }
        }

        List<ResultInstance> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            try {
                BufferedReader br = new BufferedReader(new FileReader("/Users/sergio/Proyectos IntelliJ/BPP/experiments/final_mas_tiempo_ejecucion/previo_results_paper_parameters_" + i + ".txt"));
                String line;
                ResultInstance res = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("SOLVING")) {
                        String instanceName = line.split("SOLVING \\.\\./\\.\\./instances/whole_set/")[1].replace("...", "");
                        res = new ResultInstance(instanceName, 0, 0, 0);
                        continue;
                    }
                    if (line.contains("New best")) {
                        String[] tokens = line.split(" ");
                        double newTTB = Double.parseDouble(tokens[5]);
                        assert res != null;
                        res.ttb = newTTB;
                    }
                    if (line.contains("Best found")) {
                        String[] tokens = line.split(" ");
                        double newOF = Double.parseDouble(tokens[2]);
                        assert res != null;
                        res.ofValue = newOF;
                    }
                    if (line.contains("Total running time:")) {
                        String[] tokens = line.split(" ");
                        double newTime = Double.parseDouble(tokens[3]);
                        assert res != null;
                        res.time = newTime;
                        results.add(res);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        results.sort(Comparator.comparing(ResultInstance::getInstanceName));
        String previousInstanceName = "";
        XSSFRow rowData = s.createRow(1);
        XSSFCell cellData;
        for (ResultInstance r : results) {
            if (!r.getInstanceName().equals(previousInstanceName)) {
                rowIdx++;
                rowData = s.createRow(rowIdx);
                cellData = rowData.createCell(0);
                cellData.setCellValue(r.getInstanceName());
                columnIdx = 1;

                previousInstanceName = r.getInstanceName();
            }
            cellData = rowData.createCell(columnIdx);
            cellData.setCellValue(r.ofValue);
            columnIdx++;
            cellData = rowData.createCell(columnIdx);
            cellData.setCellValue(r.time);
            columnIdx++;
            cellData = rowData.createCell(columnIdx);
            cellData.setCellValue(r.ttb);
            columnIdx++;
        }
        try {
            File outDirCreator = new File(outDir);
            //noinspection ResultOfMethodCallIgnored
            outDirCreator.mkdirs();
            wb.write(new FileOutputStream(outputFile));
            wb.close();
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

    }


}
