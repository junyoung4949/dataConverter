package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExcelColumnDto {
    private String date;      // statDt에 해당
    private String adId;      // 광고 Id
    private String impCnt;    // 노출수
    private String clkCnt;    // 클릭수
    private String salesAmt;  // 총비용
    private String ccnt;      // 전환수
    private String convAmt;   // 전환 금액 - 전환을 통해 발생한 총 매출액
}
