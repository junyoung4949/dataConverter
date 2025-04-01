import client.MainGui;
import client.panel.*;
import util.Context;
import util.JPanelAdvisor;

public class Application {
    public static void main(String[] args) {
        JPanelAdvisor panelAdvisor = new JPanelAdvisor();
        Context context = new Context();

        panelAdvisor.setNavigation(new Navigation(panelAdvisor));
        panelAdvisor.add("excelRegister", new ExcelRegister());
        panelAdvisor.add("apiInfoRegister", new ApiInfoRegister(context, panelAdvisor));
        panelAdvisor.add("excelGen", new ExcelGen());
        panelAdvisor.add("apiInfoDeleteModify", new ApiInfoDeleteModify(context));


        new MainGui(context, panelAdvisor);
    }
}
