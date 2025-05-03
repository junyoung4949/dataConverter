import client.MainGui;
import client.panel.*;
import util.Context;
import util.ComponentManager;

public class Application {
    public static void main(String[] args) {
        Context context = new Context();
        ComponentManager componentManager = context.componentManager();

        componentManager.setNavigation(new Navigation(context));
        componentManager.add("excelRegister", new ExcelRegister(context));
        componentManager.add("apiInfoRegister", new ApiInfoRegister(context));
        componentManager.add("excelGen", new ExcelGen(context));
        componentManager.add("apiInfoDeleteModify", new ApiInfoDeleteModify(context));

        componentManager.setWindow(new MainGui(context));

        Runtime rt = Runtime.getRuntime();
        System.out.println("Max Heap: " + rt.maxMemory() / (1024 * 1024) + "MB");
        System.out.println("Initial Heap: " + rt.totalMemory() / (1024 * 1024) + "MB");
    }
}
