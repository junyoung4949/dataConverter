package dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiInfoDto {
    private String name;
    private String customerId;
    private String accessLicense;
    private String secretKey;
}
