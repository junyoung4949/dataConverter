package util;

import entity.ApiInfo;

import java.util.List;

public interface ExcelService {
    void convert(ApiInfo apiInfo);
    void convertAll(List<ApiInfo> apiInfos);
}
