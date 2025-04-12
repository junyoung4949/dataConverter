package api;

import entity.ApiInfo;
import lombok.extern.slf4j.Slf4j;
import util.Context;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.net.http.HttpResponse.*;

@Slf4j
public class RestClient {

    private final String BASE_URL = "https://api.searchad.naver.com";
    private final String PATH = "/stat-reports";

    private final SignaturesGenerator signaturesGenerator;

    public RestClient(SignaturesGenerator signaturesGenerator) {
        this.signaturesGenerator = signaturesGenerator;
    }

    public void get(List<ApiInfo> apiInfos) {
        HttpClient client = HttpClient.newBuilder().build();
        List<HttpRequest> requests = apiInfos.stream().map(this::makeHttpRequest).collect(Collectors.toList());
        List<String> responseBodyList = requests.stream().map(request -> {
            HttpResponse<String> response = getResponse(request, client);
            return response.body();
        }).collect(Collectors.toList());

        log.info("response list : {}", responseBodyList);
    }

    private HttpResponse<String> getResponse(HttpRequest request, HttpClient client) {
        try {
            return client.sendAsync(request, BodyHandlers.ofString()).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest makeHttpRequest(ApiInfo apiInfo) {
        try {
            long timeStamp = System.currentTimeMillis();
            String timeStampString = String.valueOf(timeStamp);
            String signature = signaturesGenerator.generateSignature(timeStampString, "POST", PATH, apiInfo.getSecretKey());
            return HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + PATH)) // URL 객체
                    .POST(null)
                    .header("X-Timestamp", timeStampString)
                    .header("Content-Type", "application/json;")
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
