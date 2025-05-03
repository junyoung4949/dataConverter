package util.excel;

import dto.ExcelColumnDto;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.ExceptionResolver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class RawDataSheetModifier {

    private final ExceptionResolver exceptionResolver;

    public RawDataSheetModifier(ExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
    }

    public void modifyAndEncryptRawDataSheet(File inputXlsxFile, String outputEncryptedXlsxPath, String password, List<ExcelColumnDto> columnDtoList) {
        try {
            Path tempModifiedXlsx = Files.createTempFile("modified-", ".xlsx");
            modifyOnlyRawDataSheet(inputXlsxFile.toPath(), tempModifiedXlsx, columnDtoList);
            encryptXlsxWithPassword(tempModifiedXlsx.toFile(), new File(outputEncryptedXlsxPath), password);
            Files.deleteIfExists(tempModifiedXlsx);
        } catch (Exception e) {
            exceptionResolver.resolve("엑셀 디코딩중 오류 발생", e);
        }
    }

    private void modifyOnlyRawDataSheet(Path inputXlsx, Path outputXlsx, List<ExcelColumnDto> columnDtoList) throws Exception {
        try (ZipFile zipIn = new ZipFile(inputXlsx.toFile());
             ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(outputXlsx))) {

            String targetRid = findSheetRid(zipIn, "xl/workbook.xml", "raw data");
            String targetSheetPath = "xl/" + findTargetByRid(zipIn, "xl/_rels/workbook.xml.rels", targetRid);

            Enumeration<? extends ZipEntry> entries = zipIn.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();

                zipOut.putNextEntry(new ZipEntry(name));
                try (InputStream is = zipIn.getInputStream(entry)) {
                    if (name.equals(targetSheetPath)) {
                        zipOut.write(modifySheetXml(is, columnDtoList));
                    } else {
                        copyStream(is, zipOut);
                    }
                }
                zipOut.closeEntry();
            }
        }
    }

    private String findSheetRid(ZipFile zip, String workbookPath, String sheetName) throws Exception {
        try (InputStream is = zip.getInputStream(zip.getEntry(workbookPath))) {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(is);
            NodeList sheets = doc.getElementsByTagName("sheet");
            for (int i = 0; i < sheets.getLength(); i++) {
                Element sheet = (Element) sheets.item(i);
                if (sheetName.equals(sheet.getAttribute("name"))) {
                    return sheet.getAttribute("r:id");
                }
            }
        }
        throw new IllegalArgumentException("Sheet '" + sheetName + "'을(를) 찾을 수 없습니다.");
    }

    private String findTargetByRid(ZipFile zip, String relsPath, String rid) throws Exception {
        try (InputStream is = zip.getInputStream(zip.getEntry(relsPath))) {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(is);
            NodeList rels = doc.getElementsByTagName("Relationship");
            for (int i = 0; i < rels.getLength(); i++) {
                Element rel = (Element) rels.item(i);
                if (rid.equals(rel.getAttribute("Id"))) {
                    return rel.getAttribute("Target");
                }
            }
        }
        throw new IllegalArgumentException("Relationship Id='" + rid + "'에 해당하는 Target을 찾을 수 없습니다.");
    }

    private byte[] modifySheetXml(InputStream xmlIn, List<ExcelColumnDto> columnDtoList) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(xmlIn);

        Element worksheet = doc.getDocumentElement();
        NodeList sheetDataList = worksheet.getElementsByTagName("sheetData");
        Element sheetData;

        if (sheetDataList.getLength() == 0) {
            sheetData = doc.createElement("sheetData");
            worksheet.appendChild(sheetData);
        } else {
            sheetData = (Element) sheetDataList.item(0);
        }

        // 헤더: A1 ~ G1
        String[] headers = {"날짜", "광고 ID", "노출수", "클릭수", "총비용", "전환수", "전환 금액"};
        Element headerRow = doc.createElement("row");
        headerRow.setAttribute("r", "1");
        for (int i = 0; i < headers.length; i++) {
            Element cell = doc.createElement("c");
            String cellRef = (char) ('A' + i) + "1";
            cell.setAttribute("r", cellRef);
            cell.setAttribute("t", "inlineStr");
            Element is = doc.createElement("is");
            Element t = doc.createElement("t");
            t.setTextContent(headers[i]);
            is.appendChild(t);
            cell.appendChild(is);
            headerRow.appendChild(cell);
        }
        sheetData.appendChild(headerRow);

        // 데이터 행: A(i + 2) ~ G(i + 2)
        for (int i = 0; i < columnDtoList.size(); i++) {
            String[] values = {
                    columnDtoList.get(i).getDate(),
                    columnDtoList.get(i).getAdId(),
                    columnDtoList.get(i).getImpCnt(),
                    columnDtoList.get(i).getClkCnt(),
                    columnDtoList.get(i).getSalesAmt(),
                    columnDtoList.get(i).getCcnt(),
                    columnDtoList.get(i).getConvAmt()
            };
            Element dataRow = doc.createElement("row");
            dataRow.setAttribute("r", String.valueOf(i + 2));
            for (int j = 0; j < values.length; j++) {
                if (j == 1) {
                    Element cell = doc.createElement("c");
                    String cellRef = (char)('A' + j) + String.valueOf(i + 2);
                    cell.setAttribute("r", cellRef);
                    cell.setAttribute("t", "inlineStr");
                    Element is = doc.createElement("is");
                    Element t = doc.createElement("t");
                    t.setTextContent(values[j]);
                    is.appendChild(t);
                    cell.appendChild(is);
                    dataRow.appendChild(cell);
                    continue;
                }
                Element cell = doc.createElement("c");
                String cellRef = (char)('A' + j) + String.valueOf(i + 2);
                cell.setAttribute("r", cellRef);
                Element v = doc.createElement("v");
                v.setTextContent(values[j]);
                cell.appendChild(v);
                dataRow.appendChild(cell);
            }
            sheetData.appendChild(dataRow);
        }

        // DOM → byte[]
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty(OutputKeys.INDENT, "no");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tr.transform(new DOMSource(doc), new StreamResult(baos));
        return baos.toByteArray();
    }


    private void encryptXlsxWithPassword(File inputXlsx, File outputXlsx, String password) throws Exception {
        try (
                POIFSFileSystem fs = new POIFSFileSystem();
                OutputStream encryptedOut = new FileOutputStream(outputXlsx);
                FileInputStream fis = new FileInputStream(inputXlsx)
        ) {
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(password);

            try (OPCPackage opc = OPCPackage.open(fis);
                 OutputStream os = encryptor.getDataStream(fs)) {
                opc.save(os);
            }

            fs.writeFilesystem(encryptedOut);
        }
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }
}
