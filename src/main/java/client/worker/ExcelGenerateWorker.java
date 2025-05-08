package client.worker;

import dto.ExcelColumnDto;
import entity.ApiInfo;
import service.ExcelDataService;
import service.ExcelEditService;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Map;

public class ExcelGenerateWorker extends SwingWorker<Void, Integer> {

    private final List<ApiInfo> apis;
    private final String dateRange;
    private final File folder;
    private final ExcelDataService excelDataService;
    private final ExcelEditService excelEditService;
    private final JButton targetButton;
    private final JProgressBar progressBar;
    private final JComponent parentComponent;
    private final ExcelGenerateWorkerExecutor executor;

    public ExcelGenerateWorker(
            List<ApiInfo> apis,
            String dateRange,
            File folder,
            ExcelDataService excelDataService,
            ExcelEditService excelEditService,
            JButton targetButton,
            JProgressBar progressBar,
            JComponent parentComponent,
            ExcelGenerateWorkerExecutor executor
    ) {
        this.apis = apis;
        this.dateRange = dateRange;
        this.folder = folder;
        this.excelDataService = excelDataService;
        this.excelEditService = excelEditService;
        this.targetButton = targetButton;
        this.progressBar = progressBar;
        this.parentComponent = parentComponent;
        this.executor = executor;
    }

    @Override
    protected Void doInBackground() {
        try {
            SwingUtilities.invokeLater(() -> {
                progressBar.setVisible(true);
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                targetButton.setEnabled(false);
            });
            Map<String, List<ExcelColumnDto>> excelData = excelDataService.getExcelData(apis, dateRange);
            excelEditService.editAndSave(excelData, folder);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentComponent, "생성 중 오류 발생: " + ex.getMessage());
        }

        return null;
    }

    @Override
    protected void done() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            progressBar.setIndeterminate(false);
            targetButton.setEnabled(true);
            JOptionPane.showMessageDialog(parentComponent, "엑셀 생성 완료");
        });
        executor.done();
    }

    @Override
    protected void process(List<Integer> chunks) {
        int latest = chunks.get(chunks.size() - 1);
        progressBar.setValue(latest);
    }

    public void updateProgress(int progress) {
        setProgress(progress);
    }
}
