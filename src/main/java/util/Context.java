package util;

import api.HttpRequestExecutor;
import api.HttpRequestGenerator;
import api.SignaturesGenerator;
import client.worker.ExcelGenerateWorkerExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import repository.*;
import service.ExcelDataService;
import service.ExcelEditService;
import service.ExcelService;
import util.excel.PasswordProtectedExcelHandler;
import util.excel.RawDataSheetModifier;

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

    private final PasswordProtectedExcelHandler passwordProtectedExcelHandler;
    private final RawDataSheetModifier rawDataSheetModifier;

    private final ExcelGenerateWorkerExecutor excelGenerateWorkerExecutor;

    public Context() {
        this.messageDisplayer = new MessageDisplayer();
        this.exceptionResolver = new ExceptionResolver(this.messageDisplayer);

        this.passwordProtectedExcelHandler = new PasswordProtectedExcelHandler(this.exceptionResolver);
        this.rawDataSheetModifier = new RawDataSheetModifier(this.exceptionResolver);

        this.excelRepository = new DBExcelRepository();
        this.componentManager = new ComponentManager();
        this.apiInfoRepository = new DBApiInfoRepository();

        this.excelGenerateWorkerExecutor = new ExcelGenerateWorkerExecutor();
        this.signaturesGenerator = new SignaturesGenerator(this.exceptionResolver);
        this.httpRequestGenerator = new HttpRequestGenerator(this.signaturesGenerator, this.exceptionResolver);
        this.httpRequestExecutor = new HttpRequestExecutor(new ObjectMapper(), this.exceptionResolver);
        this.excelDataService = new ExcelDataService(this.httpRequestGenerator, this.httpRequestExecutor, this.exceptionResolver, excelGenerateWorkerExecutor);
        this.excelEditService = new ExcelEditService(this.excelRepository, this.exceptionResolver, this.passwordProtectedExcelHandler, this.rawDataSheetModifier, excelGenerateWorkerExecutor);
        this.reloader = new Reloader(this.componentManager);

        excelGenerateWorkerExecutor.initialize(this.excelDataService, this.excelEditService);
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

    public ExcelGenerateWorkerExecutor excelGenerateWorkerExecutor() {
        return this.excelGenerateWorkerExecutor;
    }
}
