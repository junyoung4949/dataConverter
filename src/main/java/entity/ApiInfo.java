package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiInfo {

    public ApiInfo(String name, Long customerId, String accessLicense, String secretKey) {
        this.name = name;
        this.customerId = customerId;
        this.accessLicense = accessLicense;
        this.secretKey = secretKey;
    }

    private Long id;
    private String name;
    private Long customerId;
    private String accessLicense;
    private String secretKey;
}
