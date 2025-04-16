package api;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.GeneralSecurityException;
import java.security.Security;

public class SignaturesGenerator {

    public SignaturesGenerator() {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String PROVIDER = "BC";
    private static final String HMAC_SHA256 = "HMac-SHA256";

    public String generateSignature(String timestamp, String method, String resource, String key) {
        return generateSignature(timestamp + "." + method + "." + resource, key);
    }

    public String generateSignature(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256, PROVIDER);
            mac.init(new SecretKeySpec(key.getBytes(), HMAC_SHA256));
            return DatatypeConverter.printBase64Binary(mac.doFinal(data.getBytes()));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

}
