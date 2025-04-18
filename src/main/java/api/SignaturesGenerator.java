package api;

import util.ExceptionResolver;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class SignaturesGenerator {


    private static final String HMAC_SHA256 = "HmacSHA256"; // 표준 이름
    private final ExceptionResolver exceptionResolver;


    public SignaturesGenerator(ExceptionResolver exceptionResolver) {
        this.exceptionResolver =exceptionResolver;
    }

    public String generateSignature(String timestamp, String method, String resource, String key) {
        String data = timestamp + "." + method + "." + resource;
        return generateSignature(data, key);
    }

    public String generateSignature(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256); // 기본 JDK 프로바이더 사용
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(rawHmac); // Java 8 이상
        } catch (GeneralSecurityException e) {
            exceptionResolver.resolve("signature 생성중 오류 발생", e);
        }
        return null;
    }
}