package util;

public class Reloader {

    private final ComponentManager panelAdvisor;

    public Reloader(ComponentManager panelAdvisor) {
        this.panelAdvisor = panelAdvisor;
    }

    public void reload() {
        panelAdvisor.getAll().values().forEach(panel -> {
            if (panel instanceof Reloadable) {
                Reloadable reloadable = (Reloadable) panel;
                reloadable.reload();
            }
        });
    }
}
