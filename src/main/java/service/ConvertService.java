package service;

import api.HttpRequestGenerator;
import api.SignaturesGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExcelColumnDto;
import dto.StatReportGetDto;
import dto.StatReportPostDto;
import entity.ApiInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ConvertService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpRequestGenerator httpRequestGenerator;

    public ConvertService(ObjectMapper objectMapper, HttpRequestGenerator httpRequestGenerator) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.httpRequestGenerator = httpRequestGenerator;
    }

    public void getExcel(List<ApiInfo> apiInfos, String dateRange) {

        String[] dateArray = dateRange.split("~");
        Map<String, List<ExcelColumnDto>> apiInfoExcelColumnMap = new HashMap<>();

        if (dateArray.length == 1) { // 하루치를 원하는 경우
            apiInfos.forEach(apiInfo -> {
                List<ExcelColumnDto> excelColumnDtoList = new ArrayList<>();
                Set<String> noReduplicationAdIds = getNoReduplicationAdId(apiInfo, dateArray[0]);
                noReduplicationAdIds.forEach(adId -> {
                    excelColumnDtoList.add(getExcelColumn(apiInfo, adId, dateArray[0]));
                    try {
                        Thread.sleep(70);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                apiInfoExcelColumnMap.put(apiInfo.getName() + LocalDateTime.now(), excelColumnDtoList);
            });
            try {
                for (Map.Entry<String, List<ExcelColumnDto>> stringListEntry : apiInfoExcelColumnMap.entrySet()) {
                    System.out.println("현재 엑셀의 key : " + stringListEntry.getKey());
                    List<ExcelColumnDto> currentExcel = stringListEntry.getValue();
                    for (ExcelColumnDto excelColumnDto : currentExcel) {
                        System.out.println(objectMapper.writeValueAsString(excelColumnDto));
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
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
                        try {
                            Thread.sleep(70);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                });
                apiInfoExcelColumnMap.put(apiInfo.getName() + LocalDateTime.now(), excelColumnDtoList);
            });

            try {
                for (Map.Entry<String, List<ExcelColumnDto>> stringListEntry : apiInfoExcelColumnMap.entrySet()) {
                    System.out.println("현재 엑셀의 key : " + stringListEntry.getKey());
                    List<ExcelColumnDto> currentExcel = stringListEntry.getValue();
                    for (ExcelColumnDto excelColumnDto : currentExcel) {
                        System.out.println(objectMapper.writeValueAsString(excelColumnDto));
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Set<String> getNoReduplicationAdId(ApiInfo apiInfo, String date){

        // report를 만들어달라는 요청을 보냄
        Long reportJobId = sendRequestForMakeStatReport(apiInfo, date);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 만든 report를 가져옴
        String downloadUrl = sendRequestForGetStatReport(reportJobId, apiInfo);

        // downloadUrl을 통해 tsx파일을 받아옴 -> adId를 중복을 제거해 반환함
        return sendRequestForGetTsx(downloadUrl, apiInfo);
    }

    private Long sendRequestForMakeStatReport(ApiInfo apiInfo, String date){
        try {
            HttpRequest request = httpRequestGenerator.genPostHttpRequest("/stat-reports", apiInfo, "{ \"reportTp\" : \"AD_DETAIL\", \"statDt\" : \"" + date + "\" }");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            StatReportPostDto statReportPostDto = objectMapper.readValue(response.body(), StatReportPostDto.class);
            log.info("statReportPostDto : {}", statReportPostDto);
            return statReportPostDto.getReportJobId();
        } catch (JsonMappingException e) {
            log.error("json 매핑중 예외 발생", e);
        } catch (IOException e) {
            log.error("레포트 생성 request중 입출력 예외 발생", e);
        } catch (InterruptedException e) {
            log.error("레포트 생성 request중 인터럽트 예외 발생", e);
        }
        return null;
    }

    private String sendRequestForGetStatReport(Long reportJobId, ApiInfo apiInfo){
        try {
            HttpRequest request = httpRequestGenerator.genGetHttpRequest("/stat-reports/" + reportJobId, apiInfo);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            StatReportGetDto statReportGetDto = objectMapper.readValue(response.body(), StatReportGetDto.class);
            log.info("statReportGetDto : {}", statReportGetDto);
            return statReportGetDto.getDownloadUrl();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("레포트 조회 request중 입출력 예외 발생", e);
        } catch (InterruptedException e) {
            log.error("레포트 조회 request중 인터럽트 예외 발생", e);
        }
        return null;
    }

    private Set<String> sendRequestForGetTsx(String downloadUrl, ApiInfo apiInfo){
        try {
            HttpRequest request = httpRequestGenerator.genDownloadTsxRequest(downloadUrl, apiInfo);
            InputStream tsxInputStream = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();

            // adId를 추출해 Set에 담은뒤 반환함
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
            }

            // display
            for (String val : resultSet) {
                System.out.println(val);
            }
            return resultSet;
        } catch (IOException e) {
            log.error("tsx 조회 request중 입출력 예외 발생", e);
        } catch (InterruptedException e) {
            log.error("tsx 조회 request중 인터럽트 예외 발생", e);
        }
        return null;
    }

    private ExcelColumnDto getExcelColumn(ApiInfo apiInfo, String adId, String date) {
        Map<String, String> paramMap = makeParamMap(adId, date);
        HttpRequest request = httpRequestGenerator.genGetHttpRequest("/stats", apiInfo, paramMap);
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode firstData = root.path("data").get(0);
            ExcelColumnDto excelColumnDto = objectMapper.treeToValue(firstData, ExcelColumnDto.class);
            log.info("response.body() : {}", response.body());
            excelColumnDto.setDate(date);
            excelColumnDto.setAdId(adId);
//            log.info("stat response to object : {}", excelColumnDto);
            return excelColumnDto;
        } catch (IOException e) {
            log.error("stat 조회중 입출력 예외 발생", e);
        } catch (InterruptedException e) {
            log.error("stat 조회중 인터럽트 예외 발생", e);
        }
        return null;
    }

    private static Map<String, String> makeParamMap(String adId, String date) {
        Map<String, String> param = new HashMap<>();
        param.put("id", adId);
        param.put("fields", "[\"impCnt\",\"clkCnt\",\"salesAmt\",\"ccnt\",\"convAmt\"]");
        String formattedDate = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
        param.put("timeRange", "{ \"since\" : \"" + formattedDate + "\" , \"until\" : \"" + formattedDate + "\" }");
        return param;
    }

    public static void main(String[] args) {
        ConvertService convertService = new ConvertService(new ObjectMapper(), new HttpRequestGenerator(new SignaturesGenerator()));
        ApiInfo apiInfo = new ApiInfo(
                "banana15",
                2745157L,
                "010000000031e24716604cd101e84c7450142ea86b044992c19d124315fe433c392cd9f974",
                "AQAAAAAx4kcWYEzRAehMdFAULqhrddEl8YM0PgL1vU6YYaQfmA==");
//        convertService.getNoReduplicationAdId(
//                apiInfo,
//                "20250404");
        convertService.getExcel(List.of(apiInfo), "20250403~20250404");
//        convertService.getExcelColumn(apiInfo, "nad-a001-04-000000361850342", "20250403");
    }
}