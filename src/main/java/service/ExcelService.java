package service;

import entity.ApiInfo;

import java.util.List;

public interface ExcelService {

    // excel을 저장하는 로직의 경우
    // excel 파일을 entity로 변경하는 과정이 필요하므로 service를 거치는게 좋을거 같다.
    // 또한 excel 파일을 어디에 저장할지는 이후에도 변경의 여지가 있음
    void saveExcel();
    void convert(ApiInfo apiInfo);
    void convertAll(List<ApiInfo> apiInfos);
}
