package service;

import client.worker.ExcelGenerateWorkerExecutor;
import dto.ExcelColumnDto;
import entity.Excel;
import repository.ExcelRepository;
import util.ExceptionResolver;
import util.excel.PasswordProtectedExcelHandler;
import util.excel.RawDataSheetModifier;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class ExcelEditService {

    private final ExcelRepository excelRepository;
    private final ExceptionResolver exceptionResolver;

    private final PasswordProtectedExcelHandler passwordProtectedExcelHandler;
    private final RawDataSheetModifier rawDataSheetModifier;
    private final ExcelGenerateWorkerExecutor excelGenerateWorkerExecutor;

    public ExcelEditService(ExcelRepository excelRepository, ExceptionResolver exceptionResolver, PasswordProtectedExcelHandler passwordProtectedExcelHandler, RawDataSheetModifier rawDataSheetModifier, ExcelGenerateWorkerExecutor excelGenerateWorkerExecutor) {
        this.excelRepository = excelRepository;
        this.exceptionResolver = exceptionResolver;
        this.passwordProtectedExcelHandler = passwordProtectedExcelHandler;
        this.rawDataSheetModifier = rawDataSheetModifier;
        this.excelGenerateWorkerExecutor = excelGenerateWorkerExecutor;
    }

    public void editAndSave(Map<String, List<ExcelColumnDto>> resultMap, File saveDirectory) {
        Excel excel = excelRepository.get();
        File tempFile = passwordProtectedExcelHandler.decryptToTempFile(excel.getLocation(), excel.getPassword());

        for (Map.Entry<String, List<ExcelColumnDto>> entry : resultMap.entrySet()) {
            rawDataSheetModifier.modifyAndEncryptRawDataSheet(tempFile, saveDirectory.getPath() + "/" + entry.getKey() + ".xlsx", excel.getPassword(), entry.getValue());
            excelGenerateWorkerExecutor.updateProgress();
        }
        try {
            Files.deleteIfExists(tempFile.toPath());
        } catch (IOException e) {
            exceptionResolver.resolve("tempFile 제거중 에러발생", e);
        }
    }
}
