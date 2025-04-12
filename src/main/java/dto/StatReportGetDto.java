package dto;

import lombok.Data;

@Data
public class StatReportGetDto {
    private String reportJobId;
    private String statDt;
    private String updateTm;
    private String reportTp;
    private String status;
    private String downloadUrl;
    private String regTm;
    private String loginId;
}
