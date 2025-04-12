package api;

import entity.ApiInfo;
import lombok.extern.slf4j.Slf4j;
import util.Context;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

@Slf4j
public class HttpRequestGenerator {

    private final SignaturesGenerator signaturesGenerator;
    private final String BASE_URL = "https://api.searchad.naver.com";

    public HttpRequestGenerator(SignaturesGenerator signaturesGenerator) {
        this.signaturesGenerator = signaturesGenerator;
    }

    public HttpRequest genGetHttpRequest(String resource, ApiInfo apiInfo) {
        long timeStamp = System.currentTimeMillis();
        String timeStampString = String.valueOf(timeStamp);
        String signature = signaturesGenerator.generateSignature(String.valueOf(System.currentTimeMillis()), "GET", resource, apiInfo.getSecretKey());
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
            log.error("url syntax exception : ", e);
        }
        return null;
    }

    public HttpRequest genPostHttpRequest(String resource, ApiInfo apiInfo, String body) {
        long timeStamp = System.currentTimeMillis();
        String timeStampString = String.valueOf(timeStamp);
        String signature = signaturesGenerator.generateSignature(String.valueOf(System.currentTimeMillis()), "POST", resource, apiInfo.getSecretKey());
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
            log.error("url syntax exception : ", e);
        }
        return null;
    }

    public HttpRequest genDownloadTsxRequest(String downloadUrl, ApiInfo apiInfo) {
        try {
            long timeStamp = System.currentTimeMillis();
            String timeStampString = String.valueOf(timeStamp);
            String signature = signaturesGenerator.generateSignature(String.valueOf(System.currentTimeMillis()), "GET", "/report-download", apiInfo.getSecretKey());
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
            log.error("url syntax exception : ", e);
        }
        return null;
    }
}
