package util;

import repository.ApiInfoRepository;
import repository.ExcelRepository;
import repository.MemoryApiInfoRepository;

public class Context {

    public ApiInfoRepository apiInfoRepository() {
        return new MemoryApiInfoRepository();
    }

    public ExcelRepository excelRepository() {
        return null;
    }
}
