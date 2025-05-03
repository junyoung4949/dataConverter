package util.excel;

import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import util.ExceptionResolver;

import java.io.*;

public class PasswordProtectedExcelHandler {

    private final ExceptionResolver exceptionResolver;

    public PasswordProtectedExcelHandler(ExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
    }

    public File decryptToTempFile(String encryptedXlsxPath, String password) {
        File tempDecryptedFile = null;

        try (POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(encryptedXlsxPath))) {
            // 1. 암호화 정보 읽기
            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor decryptor = Decryptor.getInstance(info);

            // 2. 암호 확인
            if (!decryptor.verifyPassword(password)) {
                throw new RuntimeException("비밀번호가 틀렸습니다.");
            }

            // 3. 암호 해제된 스트림 얻기
            try (InputStream decryptedStream = decryptor.getDataStream(fs)) {
                // 4. 임시 파일로 저장
                tempDecryptedFile = File.createTempFile("decrypted-", ".xlsx");
                try (OutputStream fos = new FileOutputStream(tempDecryptedFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = decryptedStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
            return tempDecryptedFile;
        } catch (Exception e) {
            exceptionResolver.resolve("암호화된 .xlsx 생성 중 오류 발생", e);
            return null;
        }
    }
}
