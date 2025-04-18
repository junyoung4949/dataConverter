package api;

import entity.ApiInfo;
import lombok.extern.slf4j.Slf4j;
import util.ExceptionResolver;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.util.Map;

@Slf4j
public class HttpRequestGenerator {

    private final SignaturesGenerator signaturesGenerator;
    private final ExceptionResolver exceptionResolver;

    private final String BASE_URL = "https://api.searchad.naver.com";
    private final String CHARACTER_SET = "UTF-8";

    public HttpRequestGenerator(SignaturesGenerator signaturesGenerator, ExceptionResolver exceptionResolver) {
        this.signaturesGenerator = signaturesGenerator;
        this.exceptionResolver = exceptionResolver;
    }

    public HttpRequest genGetHttpRequest(String resource, ApiInfo apiInfo) {
        long timeStamp = System.currentTimeMillis();
        String timeStampString = String.valueOf(timeStamp);
        String signature = signaturesGenerator.generateSignature(timeStampString, "GET", resource, apiInfo.getSecretKey());
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + resource))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("X-Timestamp", timeStampString)
                    .header("X-Signature", signature)
                    .header("X-Customer", String.valueOf(apiInfo.getCustomerId()))
                    .header("X-API-KEY", apiInfo.getAccessLicense())
                    .build();
        } catch (URISyntaxException e) {
            exceptionResolver.resolve("url syntax exception", e);
        }
        return null;
    }

    public HttpRequest genGetHttpRequest(String resource, ApiInfo apiInfo, Map<String, String> paramMap) {
        long timeStamp = System.currentTimeMillis();
        String timeStampString = String.valueOf(timeStamp);
        String signature = signaturesGenerator.generateSignature(timeStampString, "GET", resource, apiInfo.getSecretKey());
        String parameter = getParameter(paramMap);
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + resource + parameter))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("X-Timestamp", timeStampString)
                    .header("X-Signature", signature)
                    .header("X-Customer", String.valueOf(apiInfo.getCustomerId()))
                    .header("X-API-KEY", apiInfo.getAccessLicense())
                    .build();
        } catch (URISyntaxException e) {
            exceptionResolver.resolve("url syntax exception", e);
        }
        return null;
    }

    public HttpRequest genPostHttpRequest(String resource, ApiInfo apiInfo, String body) {
        long timeStamp = System.currentTimeMillis();
        String timeStampString = String.valueOf(timeStamp);
        String signature = signaturesGenerator.generateSignature(timeStampString, "POST", resource, apiInfo.getSecretKey());
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + resource))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .header("X-Timestamp", timeStampString)
                    .header("X-Signature", signature)
                    .header("X-Customer", String.valueOf(apiInfo.getCustomerId()))
                    .header("X-API-KEY", apiInfo.getAccessLicense())
                    .build();
        } catch (URISyntaxException e) {
            exceptionResolver.resolve("url syntax exception", e);
        }
        return null;
    }

    public HttpRequest genDownloadTsxRequest(String downloadUrl, ApiInfo apiInfo) {
        try {
            long timeStamp = System.currentTimeMillis();
            String timeStampString = String.valueOf(timeStamp);
            String signature = signaturesGenerator.generateSignature(timeStampString, "GET", "/report-download", apiInfo.getSecretKey());
            return HttpRequest.newBuilder()
                    .uri(new URI(downloadUrl))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("X-Timestamp", timeStampString)
                    .header("X-Signature", signature)
                    .header("X-Customer", String.valueOf(apiInfo.getCustomerId()))
                    .header("X-API-KEY", apiInfo.getAccessLicense())
                    .build();
        } catch (URISyntaxException e) {
            exceptionResolver.resolve("url syntax exception", e);
        }
        return null;
    }

    private String getParameter(Map<String, String> paramMap) {
        StringBuilder parameter = new StringBuilder("?");
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            try {
                parameter.append(URLEncoder.encode(entry.getKey(), CHARACTER_SET))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), CHARACTER_SET))
                        .append("&");
            } catch (UnsupportedEncodingException e) {
                exceptionResolver.resolve("url encode중 exception 발생", e);
            }
        }
        if (parameter.length() > 1) {
            parameter.setLength(parameter.length() - 1); // 마지막 & 제거
        }
        return parameter.toString();
    }
}
