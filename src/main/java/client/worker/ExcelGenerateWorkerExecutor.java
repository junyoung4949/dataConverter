package client.worker;

import entity.ApiInfo;
import service.ExcelDataService;
import service.ExcelEditService;

import javax.swing.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Set;

public class ExcelGenerateWorkerExecutor {

    private ExcelDataService excelDataService;
    private ExcelEditService excelEditService;

    private ExcelGenerateWorker worker;
    private Integer maxProgress = 0;
    private Integer currentProgress = 0;

    public void initialize(ExcelDataService excelDataService, ExcelEditService excelEditService) {
        this.excelDataService = excelDataService;
        this.excelEditService = excelEditService;
    }


    public void excelGenExecute(Set<ApiInfo> selectedItems,
                                String dateRange, File selectedFolder,
                                JButton excelGenBtn,
                                JProgressBar progressBar,
                                JPanel parentComponent) {
        this.worker = new ExcelGenerateWorker(
                new ArrayList<>(selectedItems),
                dateRange,
                selectedFolder,
                this.excelDataService,
                this.excelEditService,
                excelGenBtn,
                progressBar,
                parentComponent,
                this
        );
        this.worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                progressBar.setValue(progress);
            }
        });

        String[] dateArray = dateRange.split("~");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (dateArray.length == 1) {
            this.maxProgress = selectedItems.size() * 2;
        } else {
            LocalDate startDate = LocalDate.parse(dateArray[0], formatter);
            LocalDate endDate = LocalDate.parse(dateArray[1], formatter);
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                maxProgress += selectedItems.size();
            }
            maxProgress += selectedItems.size();
        }
        this.worker.execute();
    }

    public void updateProgress() {
        worker.updateProgress((int) (++currentProgress / (double) maxProgress * 100));
    }

    public void done() {
        this.maxProgress = 0;
        this.currentProgress = 0;
        this.worker = null;
    }
}
