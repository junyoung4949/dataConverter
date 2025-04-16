package service;

import api.HttpRequestExecutor;
import api.HttpRequestGenerator;
import dto.ExcelColumnDto;
import dto.StatReportGetDto;
import dto.StatReportPostDto;
import entity.ApiInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ExcelDataService {

    private final HttpRequestGenerator httpRequestGenerator;
    private final HttpRequestExecutor httpRequestExecutor;

    public ExcelDataService(HttpRequestGenerator httpRequestGenerator, HttpRequestExecutor httpRequestExecutor) {
        this.httpRequestGenerator = httpRequestGenerator;
        this.httpRequestExecutor = httpRequestExecutor;
    }

    public Map<String, List<ExcelColumnDto>> getExcelData(List<ApiInfo> apiInfos, String dateRange) {

        String[] dateArray = dateRange.split("~");
        Map<String, List<ExcelColumnDto>> resultMap = new HashMap<>();

        if (dateArray.length == 1) { // 하루치를 원하는 경우
            apiInfos.forEach(apiInfo -> {
                List<ExcelColumnDto> excelColumnDtoList = new ArrayList<>();
                Set<String> noReduplicationAdIds = getNoReduplicationAdId(apiInfo, dateArray[0]);
                noReduplicationAdIds.forEach(adId -> {
                    excelColumnDtoList.add(getExcelColumn(apiInfo, adId, dateArray[0]));
                });
                resultMap.put(apiInfo.getName() + LocalDateTime.now(), excelColumnDtoList);
            });
            return resultMap;
        } else { // 여러날짜를 원하는 경우
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            // 시작일, 종료일 분리
            LocalDate startDate = LocalDate.parse(dateArray[0], formatter);
            LocalDate endDate = LocalDate.parse(dateArray[1], formatter);

            // 날짜를 저장할 리스트
            List<String> dateList = new ArrayList<>();

            // 시작일부터 종료일까지 하루씩 증가하며 리스트에 추가
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                dateList.add(current.format(formatter));
                current = current.plusDays(1);
            }

            apiInfos.forEach(apiInfo -> {
                List<ExcelColumnDto> excelColumnDtoList = new ArrayList<>();
                dateList.forEach(date -> {
                    Set<String> noReduplicationAdIds = getNoReduplicationAdId(apiInfo, date);
                    noReduplicationAdIds.forEach(adId -> {
                        excelColumnDtoList.add(getExcelColumn(apiInfo, adId, date));
                    });
                });
                resultMap.put(apiInfo.getName() + LocalDateTime.now(), excelColumnDtoList);
            });
            return resultMap;
        }
    }

    private Set<String> getNoReduplicationAdId(ApiInfo apiInfo, String date){

        // report를 만들어달라는 요청을 보냄
        Long reportJobId = sendRequestForMakeStatReport(apiInfo, date);

        // 만든 report를 가져옴
        String downloadUrl = sendRequestForGetStatReport(reportJobId, apiInfo);

        // downloadUrl을 통해 tsx파일을 받아옴 -> adId를 중복을 제거해 반환함
        return sendRequestForGetTsx(downloadUrl, apiInfo);
    }

    private Long sendRequestForMakeStatReport(ApiInfo apiInfo, String date){
        HttpRequest request = httpRequestGenerator.genPostHttpRequest("/stat-reports", apiInfo, "{ \"reportTp\" : \"AD_DETAIL\", \"statDt\" : \"" + date + "\" }");
        StatReportPostDto response = httpRequestExecutor.sendForObjectWithRetry(request, StatReportPostDto.class);
        return response.getReportJobId();
    }

    private String sendRequestForGetStatReport(Long reportJobId, ApiInfo apiInfo){
        HttpRequest request = httpRequestGenerator.genGetHttpRequest("/stat-reports/" + reportJobId, apiInfo);
        StatReportGetDto response = httpRequestExecutor.sendForStatReportPostDtoWithRetry(request);
        return response.getDownloadUrl();
    }

    private Set<String> sendRequestForGetTsx(String downloadUrl, ApiInfo apiInfo){
        HttpRequest request = httpRequestGenerator.genDownloadTsxRequest(downloadUrl, apiInfo);
        InputStream tsxInputStream = httpRequestExecutor.sendForInputStreamWithRetry(request);
        Set<String> resultSet = new HashSet<>();
        Pattern pattern = Pattern.compile("nad-\\S+");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(tsxInputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    resultSet.add(matcher.group());
                }
            }
        } catch (IOException e) {
            log.error("inputStream에서 adIds를 가져오는 중 입출력 예외 발생 : ", e);
        }
        return resultSet;
    }

    private ExcelColumnDto getExcelColumn(ApiInfo apiInfo, String adId, String date) {
        Map<String, String> paramMap = makeParamMap(adId, date);
        HttpRequest request = httpRequestGenerator.genGetHttpRequest("/stats", apiInfo, paramMap);
        return httpRequestExecutor.sendForExcelColumnDtoWithRetry(request, date, adId);
    }

    private static Map<String, String> makeParamMap(String adId, String date) {
        Map<String, String> param = new HashMap<>();
        param.put("id", adId);
        param.put("fields", "[\"impCnt\",\"clkCnt\",\"salesAmt\",\"ccnt\",\"convAmt\"]");
        String formattedDate = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
        param.put("timeRange", "{ \"since\" : \"" + formattedDate + "\" , \"until\" : \"" + formattedDate + "\" }");
        return param;
    }

}