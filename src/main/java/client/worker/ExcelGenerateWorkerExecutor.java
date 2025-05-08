package client.worker;

import entity.ApiInfo;
import service.ExcelDataService;
import service.ExcelEditService;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class ExcelGenerateWorkerExecutor {

    private ExcelDataService excelDataService;
    private ExcelEditService excelEditService;

    private ExcelGenerateWorker worker;
    private Integer maxProgress;
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
        this.maxProgress = selectedItems.size() * 2;
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
