package util;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class JPanelAdvisor {

    @Getter
    @Setter
    private JPanel navigation;
    private final Map<String, JPanel> store = new HashMap<>();

    public void add(String panelName, JPanel panel) {
        panel.setVisible(false);
        this.store.put(panelName, panel);
    }

    public JPanel get(String panelName) {
        return store.get(panelName);
    }

    public Map<String, JPanel> getAll() {
        return store;
    }
}
