package service;

import api.HttpRequestExecutor;
import api.HttpRequestGenerator;
import client.worker.ExcelGenerateWorkerExecutor;
import dto.ExcelColumnDto;
import dto.StatReportGetDto;
import dto.StatReportPostDto;
import entity.ApiInfo;
import lombok.extern.slf4j.Slf4j;
import util.ExceptionResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ExcelDataService {

    private final HttpRequestGenerator httpRequestGenerator;
    private final HttpRequestExecutor httpRequestExecutor;
    private final ExceptionResolver exceptionResolver;
    private final ExcelGenerateWorkerExecutor excelGenerateWorkerExecutor;

    public ExcelDataService(HttpRequestGenerator httpRequestGenerator, HttpRequestExecutor httpRequestExecutor, ExceptionResolver exceptionResolver, ExcelGenerateWorkerExecutor excelGenerateWorkerExecutor) {
        this.httpRequestGenerator = httpRequestGenerator;
        this.httpRequestExecutor = httpRequestExecutor;
        this.exceptionResolver = exceptionResolver;
        this.excelGenerateWorkerExecutor = excelGenerateWorkerExecutor;
    }

    public Map<String, List<ExcelColumnDto>> getExcelData(List<ApiInfo> apiInfos, String dateRange) {
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(apiInfos.size(), 10)); // 최대 10개 쓰레드 사용
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Map<String, List<ExcelColumnDto>> resultMap = Collections.synchronizedMap(new HashMap<>());

        String[] dateArray = dateRange.split("~");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        List<String> dateList;
        if (dateArray.length == 1) {
            dateList = List.of(dateArray[0]);
        } else {
            LocalDate startDate = LocalDate.parse(dateArray[0], formatter);
            LocalDate endDate = LocalDate.parse(dateArray[1], formatter);
            dateList = new ArrayList<>();
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                dateList.add(date.format(formatter));
            }
        }

        // apiInfo 별로 비동기 요청
        for (ApiInfo apiInfo : apiInfos) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<ExcelColumnDto> excelColumnDtoList = new ArrayList<>();
                for (String date : dateList) {
                    Set<String> adIds = getNoReduplicationAdId(apiInfo, date);
                    for (String adId : adIds) {
                        ExcelColumnDto dto = getExcelColumn(apiInfo, adId, date);
                        if (dto != null) {
                            excelColumnDtoList.add(dto);
                        }
                    }
                }
                resultMap.put(makeFileName(apiInfo), excelColumnDtoList);
                excelGenerateWorkerExecutor.updateProgress();
            }, executor);
            futures.add(future);
        }

        // 모든 작업이 끝날 때까지 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        return resultMap;
    }

    private String makeFileName(ApiInfo apiInfo) {
        return apiInfo.getName() + LocalDateTime.now().toString().substring(0, 10) + UUID.randomUUID().toString().substring(0, 7);
    }

    private Set<String> getNoReduplicationAdId(ApiInfo apiInfo, String date){

        // report를 만들어달라는 요청을 보냄
        Long reportJobId = sendRequestForMakeStatReport(apiInfo, date);

        // 만든 report를 가져옴
        String downloadUrl = sendRequestForGetStatReport(reportJobId, apiInfo);

        // report 삭제
        deleteStatReport(reportJobId, apiInfo);

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
            exceptionResolver.resolve("inputStream에서 adIds를 가져오는 중 입출력 예외 발생", e);
        }
        log.info("downloadUrl 에서 adIdSet 가져옴, downloadUrl : {}", downloadUrl);
        return resultSet;
    }

    private void deleteStatReport(Long reportJobId, ApiInfo apiInfo) {
//        HttpRequest request = httpRequestGenerator.getDeleteStatReportsRequest(reportJobId, apiInfo);
//        httpRequestExecutor.sendForDeleteStatReportsWithRetry(request);
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