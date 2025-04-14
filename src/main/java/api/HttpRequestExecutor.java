package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExcelColumnDto;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class HttpRequestExecutor {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Integer MAX_ATTEMPT = 3;

    public HttpRequestExecutor(ObjectMapper objectMapper, HttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public <T> T sendForObjectWithRetry(HttpRequest request, Class<T> responseType) {
        int attempt = 0;
        String uri = request.uri().toString();

        while (attempt < MAX_ATTEMPT) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    String body = response.body();
                    return objectMapper.readValue(body, responseType);
                } else {
                    log.warn("{} 요청 실패 : 상태코드: {}, (재시도 {}/{})", uri, statusCode, attempt + 1, MAX_ATTEMPT);
                }
            } catch (IOException e) {
                log.warn("{} 요청시도중 입출력 예외 발생 : (재시도 {}/{})", uri, attempt + 1, MAX_ATTEMPT);
            } catch (InterruptedException e) {
                log.warn("{} 요청시도중 인터럽트 예외 발생 : (재시도 {}/{})", uri, attempt + 1, MAX_ATTEMPT);
            }

            attempt++;

            // 요청 재시도 할때, delay를 얼마나 잡을지
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        log.error("{} 요청 실패 : 최대요청횟수 초과함", uri);

        // 뒤에 어떤 예외처리를 해야함
        return null;
    }

    public InputStream sendForInputStreamWithRetry(HttpRequest request) {
        int attempt = 0;
        String uri = request.uri().toString();

        while (attempt < MAX_ATTEMPT) {
            try {
                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                int statusCode = response.statusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    return response.body();
                } else {
                    log.warn("{} 요청 실패 : 상태코드: {}, (재시도 {}/{})", uri, statusCode, attempt + 1, MAX_ATTEMPT);
                }
            } catch (IOException e) {
                log.warn("{} 요청시도중 입출력 예외 발생 : (재시도 {}/{})", uri, attempt + 1, MAX_ATTEMPT);
            } catch (InterruptedException e) {
                log.warn("{} 요청시도중 인터럽트 예외 발생 : (재시도 {}/{})", uri, attempt + 1, MAX_ATTEMPT);
            }

            attempt++;

            // 요청 재시도 할때, delay를 얼마나 잡을지
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        log.error("{} 요청 실패 : 최대요청횟수 초과함", uri);

        // 뒤에 어떤 예외처리를 해야함
        return null;
    }

    public ExcelColumnDto sendForExcelColumnDtoWithRetry(HttpRequest request, String date, String adId) {
        int attempt = 0;
        String uri = request.uri().toString();

        while (attempt < MAX_ATTEMPT) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode firstData = root.path("data").get(0);
                    ExcelColumnDto excelColumnDto = objectMapper.treeToValue(firstData, ExcelColumnDto.class);
                    log.info("response.body() : {}", response.body());
                    excelColumnDto.setDate(date);
                    excelColumnDto.setAdId(adId);
                } else {
                    log.warn("{} 요청 실패 : 상태코드: {}, (재시도 {}/{})", uri, statusCode, attempt + 1, MAX_ATTEMPT);
                }
            } catch (IOException e) {
                log.warn("{} 요청시도중 입출력 예외 발생 : (재시도 {}/{})", uri, attempt + 1, MAX_ATTEMPT);
            } catch (InterruptedException e) {
                log.warn("{} 요청시도중 인터럽트 예외 발생 : (재시도 {}/{})", uri, attempt + 1, MAX_ATTEMPT);
            }

            attempt++;

            // 요청 재시도 할때, delay를 얼마나 잡을지
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        log.error("{} 요청 실패 : 최대요청횟수 초과함", uri);

        // 뒤에 어떤 예외처리를 해야함
        return null;
    }
}
