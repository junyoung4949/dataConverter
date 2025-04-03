package util;

import repository.ApiInfoRepository;
import repository.ExcelRepository;
import repository.MemoryApiInfoRepository;
import repository.MemoryExcelRepository;
import service.ExcelService;

public class Context {

    private final ComponentManager componentManager;
    private final ApiInfoRepository apiInfoRepository;
    private final ExcelRepository excelRepository;
    private final Reloader reloader;
    private final ExcelService excelService;

    public Context() {
        this.componentManager = new ComponentManager();
        this.apiInfoRepository = new MemoryApiInfoRepository();
        this.excelRepository = new MemoryExcelRepository();
        this.reloader = new Reloader(this.componentManager);
        this.excelService = null;
    }

    public ComponentManager componentManager() {
        return this.componentManager;
    }

    public ApiInfoRepository apiInfoRepository() {
        return this.apiInfoRepository;
    }

    public ExcelRepository excelRepository() {
        return this.excelRepository;
    }

    public Reloader reloader() {
        return this.reloader;
    }

    public ExcelService excelService() {
        return this.excelService;
    }
}
