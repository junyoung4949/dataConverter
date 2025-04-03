package client;

import util.Context;
import util.ComponentManager;

import javax.swing.*;
import java.awt.*;

public class MainGui extends JFrame {

    private final ComponentManager componentManager;

    public MainGui(Context context) {
        this.componentManager = context.componentManager();

        this.setting();
        this.setComponent();
        this.setVisible(true); // gui 켜기
    }

    private void setting() {
        this.setResizable(false);
        this.setPreferredSize(new Dimension(840, 840/12*9));
        this.setSize(600, 600/12*9);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
    }

    private void setComponent() {
        this.add(componentManager.getNavigation(), BorderLayout.NORTH);
        JPanel cardPanel = new JPanel(new CardLayout());
        componentManager.getAll().values().forEach(panel -> {
            cardPanel.add(panel, BorderLayout.CENTER);
        });
        add(cardPanel, BorderLayout.CENTER);
    }
}
