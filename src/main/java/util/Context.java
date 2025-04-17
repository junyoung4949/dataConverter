package util;

import api.HttpRequestExecutor;
import api.HttpRequestGenerator;
import api.SignaturesGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import repository.*;
import service.ExcelDataService;
import service.ExcelEditService;
import service.ExcelService;

public class Context {

    private final ComponentManager componentManager;
    private final ApiInfoRepository apiInfoRepository;
    private final ExcelRepository excelRepository;
    private final Reloader reloader;

    private final HttpRequestGenerator httpRequestGenerator;
    private final HttpRequestExecutor httpRequestExecutor;
    private final SignaturesGenerator signaturesGenerator;
    private final ExcelDataService excelDataService;
    private final ExcelEditService excelEditService;

    private final ExceptionResolver exceptionResolver;
    private final MessageDisplayer messageDisplayer;

    public Context() {
        this.messageDisplayer = new MessageDisplayer();
        this.exceptionResolver = new ExceptionResolver(this.messageDisplayer);

        this.excelRepository = new DBExcelRepository();
        this.componentManager = new ComponentManager();
        this.apiInfoRepository = new DBApiInfoRepository();

        this.signaturesGenerator = new SignaturesGenerator(this.exceptionResolver);
        this.httpRequestGenerator = new HttpRequestGenerator(this.signaturesGenerator, this.exceptionResolver);
        this.httpRequestExecutor = new HttpRequestExecutor(new ObjectMapper(), this.exceptionResolver);
        this.excelDataService = new ExcelDataService(this.httpRequestGenerator, this.httpRequestExecutor, this.exceptionResolver);
        this.excelEditService = new ExcelEditService(this.excelRepository, this.exceptionResolver);
        this.reloader = new Reloader(this.componentManager);


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

    public ExcelDataService excelDataService() {
        return this.excelDataService;
    }

    public ExcelEditService excelEditService() {
        return this.excelEditService;
    }
}
