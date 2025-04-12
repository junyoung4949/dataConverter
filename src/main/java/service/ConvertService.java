package service;

import api.SignaturesGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.StatReportGetDto;
import dto.StatReportPostDto;
import entity.ApiInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ConvertService {

    private final HttpClient httpClient;
    private final SignaturesGenerator signaturesGenerator;
    private final ObjectMapper objectMapper;

    public ConvertService(SignaturesGenerator signaturesGenerator, ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.signaturesGenerator = signaturesGenerator;
        this.objectMapper = objectMapper;
    }

    public void getExcel(List<ApiInfo> apiInfos, String dateRange) {

        String[] dateArray = dateRange.split("~");

        if (dateArray.length == 1) { // 하루치를 원하는 경우

        } else { // 여러날짜를 원하는 경우
            // 날짜 포맷 지정
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            // 시작일, 종료일 분리
            LocalDate startDate = LocalDate.parse(dateArray[0], formatter);
            LocalDate endDate = LocalDate.parse(dateArray[1], formatter);

            // 시작일부터 종료일까지 하루씩 증가하며 출력
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                System.out.println(current.format(formatter));
                current = current.plusDays(1);
            }

        }

        // api Info하나당 요청
        apiInfos.stream().map(apiInfo -> {

            return null;
        }).collect(Collectors.toList());
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
            long timeStamp = System.currentTimeMillis();
            String timeStampString = String.valueOf(timeStamp);
            String signature = signaturesGenerator.generateSignature(String.valueOf(System.currentTimeMillis()), "POST", "/stat-reports", apiInfo.getSecretKey());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.searchad.naver.com" + "/stat-reports"))
                    .POST(HttpRequest.BodyPublishers.ofString("{ \"reportTp\" : \"AD_DETAIL\", \"statDt\" : \"" + date + "\" }"))
                    .header("Content-Type", "application/json")
                    .header("X-Timestamp", timeStampString)
                    .header("X-Signature", signature)
                    .header("X-Customer", String.valueOf(apiInfo.getCustomerId()))
                    .header("X-API-KEY", apiInfo.getAccessLicense())
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            StatReportPostDto statReportPostDto = objectMapper.readValue(response.body(), StatReportPostDto.class);
            log.info("statReportPostDto : {}", statReportPostDto);
            return statReportPostDto.getReportJobId();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            log.error("레포트 생성 request중 url 문법 예외 발생 ", e);
        } catch (IOException e) {
            log.error("레포트 생성 request중 입출력 예외 발생", e);
        } catch (InterruptedException e) {
            log.error("레포트 생성 request중 인터럽트 예외 발생", e);
        }
        return null;
    }

    private String sendRequestForGetStatReport(Long reportJobId, ApiInfo apiInfo){
        try {
            long timeStamp = System.currentTimeMillis();
            String timeStampString = String.valueOf(timeStamp);
            String signature = signaturesGenerator.generateSignature(String.valueOf(System.currentTimeMillis()), "GET", "/stat-reports/" + reportJobId, apiInfo.getSecretKey());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.searchad.naver.com" + "/stat-reports/" + reportJobId))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("X-Timestamp", timeStampString)
                    .header("X-Signature", signature)
                    .header("X-Customer", String.valueOf(apiInfo.getCustomerId()))
                    .header("X-API-KEY", apiInfo.getAccessLicense())
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            StatReportGetDto statReportGetDto = objectMapper.readValue(response.body(), StatReportGetDto.class);
            log.info("statReportGetDto : {}", statReportGetDto);
            return statReportGetDto.getDownloadUrl();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            log.error("레포트 조회 request중 url 문법 예외 발생", e);
        } catch (IOException e) {
            log.error("레포트 조회 request중 입출력 예외 발생", e);
        } catch (InterruptedException e) {
            log.error("레포트 조회 request중 인터럽트 예외 발생", e);
        }
        return null;
    }

    private Set<String> sendRequestForGetTsx(String downloadUrl, ApiInfo apiInfo){
        try {
            long timeStamp = System.currentTimeMillis();
            String timeStampString = String.valueOf(timeStamp);
            String signature = signaturesGenerator.generateSignature(String.valueOf(System.currentTimeMillis()), "GET", "/report-download", apiInfo.getSecretKey());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(downloadUrl))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("X-Timestamp", timeStampString)
                    .header("X-Signature", signature)
                    .header("X-Customer", String.valueOf(apiInfo.getCustomerId()))
                    .header("X-API-KEY", apiInfo.getAccessLicense())
                    .build();
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
        } catch (URISyntaxException e) {
            log.error("tsx 조회 request중 url 문법 예외 발생", e);
        } catch (IOException e) {
            log.error("tsx 조회 request중 입출력 예외 발생", e);
        } catch (InterruptedException e) {
            log.error("tsx 조회 request중 인터럽트 예외 발생", e);
        }
        return null;
    }

    public static void main(String[] args) {
        ConvertService convertService = new ConvertService(new SignaturesGenerator(), new ObjectMapper());
        convertService.getNoReduplicationAdId(
                new ApiInfo(
                    "testName",
                    2745157L,
                    "010000000031e24716604cd101e84c7450142ea86b044992c19d124315fe433c392cd9f974",
                    "AQAAAAAx4kcWYEzRAehMdFAULqhrddEl8YM0PgL1vU6YYaQfmA=="),
                "20250404");
    }


}
