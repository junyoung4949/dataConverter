package service;

import dto.ExcelColumnDto;
import entity.Excel;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import repository.ExcelRepository;
import util.ExceptionResolver;
import util.MessageDisplayer;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

public class ExcelEditService {

    private final ExcelRepository excelRepository;
    private final ExceptionResolver exceptionResolver;

    public ExcelEditService(ExcelRepository excelRepository, ExceptionResolver exceptionResolver) {
        this.excelRepository = excelRepository;
        this.exceptionResolver = exceptionResolver;
    }

    public void editAndSave(Map<String, List<ExcelColumnDto>> resultMap, File saveDirectory) {
        Excel excel = excelRepository.get();

        for (Map.Entry<String, List<ExcelColumnDto>> entry : resultMap.entrySet()) {
            String fileName = entry.getKey();
            List<ExcelColumnDto> dataList = entry.getValue();

            try (
                    FileInputStream fis = new FileInputStream(excel.getLocation());
                    POIFSFileSystem fsEach = new POIFSFileSystem(fis)
            ) {
                EncryptionInfo info = new EncryptionInfo(fsEach);
                Decryptor decryptor = Decryptor.getInstance(info);

                if (!decryptor.verifyPassword(excel.getPassword())) {
                    exceptionResolver.resolve("비밀번호가 틀렸습니다", new RuntimeException("비밀번호가 틀렸습니다."));
                }

                try (InputStream decryptedStream = decryptor.getDataStream(fsEach)) {
                    Workbook workbook = new XSSFWorkbook(decryptedStream);
                    Sheet rawDataSheet = workbook.getSheet("row data");

                    if (rawDataSheet == null) {
                        exceptionResolver.resolve("raw data 이름을 가진 sheet가 존재하지 않음", new RuntimeException("raw data 이름을 가진 sheet가 존재하지 않음"));
                    }

                    Row headerRow = rawDataSheet.createRow(0);
                    headerRow.createCell(0).setCellValue("일별");
                    headerRow.createCell(1).setCellValue("광고 id");
                    headerRow.createCell(2).setCellValue("노출수");
                    headerRow.createCell(3).setCellValue("클릭수");
                    headerRow.createCell(4).setCellValue("총비용");
                    headerRow.createCell(5).setCellValue("전환수");
                    headerRow.createCell(6).setCellValue("전환 금액");

                    int rowIndex = 1;
                    for (ExcelColumnDto dto : dataList) {
                        Row row = rawDataSheet.createRow(rowIndex++);
                        row.createCell(0).setCellValue(dto.getDate());
                        row.createCell(1).setCellValue(dto.getAdId());
                        row.createCell(2).setCellValue(dto.getImpCnt());
                        row.createCell(3).setCellValue(dto.getClkCnt());
                        row.createCell(4).setCellValue(dto.getSalesAmt());
                        row.createCell(5).setCellValue(dto.getCcnt());
                        row.createCell(6).setCellValue(dto.getConvAmt());
                    }

                    File outFile = new File(saveDirectory, fileName + ".xlsx");

                    POIFSFileSystem encryptedFS = new POIFSFileSystem();
                    EncryptionInfo encInfo = new EncryptionInfo(EncryptionMode.standard);
                    Encryptor encryptor = encInfo.getEncryptor();
                    encryptor.confirmPassword(excel.getPassword());

                    try (OutputStream encryptedOut = encryptor.getDataStream(encryptedFS)) {
                        workbook.write(encryptedOut);
                    }

                    try (FileOutputStream fileOut = new FileOutputStream(outFile)) {
                        encryptedFS.writeFilesystem(fileOut);
                    }
                }
            } catch (IOException e) {
                exceptionResolver.resolve("파일 읽는중 입출력 예외 발생", e);
            } catch (GeneralSecurityException e) {
                exceptionResolver.resolve("파일 복호화 중 예외 발생", e);
            }
        }
    }
}
