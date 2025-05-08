package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExcelColumnDto;
import dto.StatReportGetDto;
import lombok.extern.slf4j.Slf4j;
import util.ExceptionResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class HttpRequestExecutor {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ExceptionResolver exceptionResolver;

    private final Integer MAX_ATTEMPT = 6;

    public HttpRequestExecutor(ObjectMapper objectMapper, ExceptionResolver exceptionResolver) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.exceptionResolver = exceptionResolver;
    }

    public <T> T sendForObjectWithRetry(HttpRequest request, Class<T> responseType) {
        int attempt = 0;
        int statusCode = 0;
        String uri = request.uri().toString();

        while (attempt < MAX_ATTEMPT) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                statusCode = response.statusCode();

                if (200 <= statusCode && statusCode < 300) {
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

        exceptionResolver.resolve("최대 요청 개수 초과함", new RuntimeException(uri + " 요청 실패, statusCode : " + statusCode));
        return null;
    }

    public StatReportGetDto sendForStatReportPostDtoWithRetry(HttpRequest request) {
        int attempt = 0;
        int statusCode = 0;
        String uri = request.uri().toString();

        while (attempt < MAX_ATTEMPT) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                statusCode = response.statusCode();
                if (200 <= statusCode && statusCode < 300) {
                    String body = response.body();
                    StatReportGetDto statReportPostDto = objectMapper.readValue(body, StatReportGetDto.class);
                    if (statReportPostDto.getStatus().equals("BUILT")) {
                        return statReportPostDto;
                    } else if (statReportPostDto.getStatus().equals("NONE")) {
                        break;
                    } else {
                        log.warn("{} 요청 재시도 ( 레포트가 완성되지 않음 ) , status : {}", uri, statReportPostDto.getStatus());
                    }
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (attempt == MAX_ATTEMPT) {
            exceptionResolver.resolve("최대 요청 개수 초과함", new RuntimeException(uri + " 요청 실패, statusCode : " + statusCode));
        } else {
            exceptionResolver.resolve("레포트 생성 불가", new RuntimeException(uri + " 요청 실패, statusCode : " + statusCode));
        }
        return null;
    }

    public InputStream sendForInputStreamWithRetry(HttpRequest request) {
        int attempt = 0;
        int statusCode = 0;
        String uri = request.uri().toString();

        while (attempt < MAX_ATTEMPT) {
            try {
                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                statusCode = response.statusCode();

                if (200 <= statusCode && statusCode < 300) {
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

        exceptionResolver.resolve("최대 요청 개수 초과함", new RuntimeException(uri + " 요청 실패, statusCode : " + statusCode));
        return null;
    }

    public ExcelColumnDto sendForExcelColumnDtoWithRetry(HttpRequest request, String date, String adId) {
        int attempt = 0;
        int statusCode = 0;
        String uri = request.uri().toString();

        while (attempt < MAX_ATTEMPT) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                statusCode = response.statusCode();

                if (200 <= statusCode && statusCode < 300) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode firstData = root.path("data").get(0);
                    ExcelColumnDto excelColumnDto = objectMapper.treeToValue(firstData, ExcelColumnDto.class);
                    excelColumnDto.setDate(date);
                    excelColumnDto.setAdId(adId);
                    return excelColumnDto;
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

        exceptionResolver.resolve("최대 요청 개수 초과함", new RuntimeException(uri + " 요청 실패, statusCode : " + statusCode));
        return null;
    }

    public void sendForDeleteStatReportsWithRetry(HttpRequest request) {
        int attempt = 0;
        int statusCode = 0;
        String uri = request.uri().toString();

        while (attempt < MAX_ATTEMPT) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                statusCode = response.statusCode();

                if (200 <= statusCode && statusCode < 300) {
                    log.info("{} 삭제 요청 성공,", uri);
                    return;
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

        exceptionResolver.resolve("최대 요청 개수 초과함", new RuntimeException(uri + " 요청 실패, statusCode : " + statusCode));
    }
}
