package util;

import api.RestClient;
import api.SignaturesGenerator;
import repository.*;
import service.ExcelService;

public class Context {

    private final ComponentManager componentManager;
    private final ApiInfoRepository apiInfoRepository;
    private final ExcelRepository excelRepository;
    private final Reloader reloader;
    private final ExcelService excelService;
    private final RestClient restClient;
    private final SignaturesGenerator signaturesGenerator;

    public Context() {
        this.componentManager = new ComponentManager();
        this.apiInfoRepository = new DBApiInfoRepository();
        this.excelRepository = new DBExcelRepository();
        this.reloader = new Reloader(this.componentManager);
        this.excelService = null;
        this.signaturesGenerator = new SignaturesGenerator();
        this.restClient = new RestClient(this.signaturesGenerator);
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
