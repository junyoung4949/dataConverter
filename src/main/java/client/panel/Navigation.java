package client.panel;

import util.Context;
import util.ComponentManager;

import javax.swing.*;
import java.awt.*;

public class Navigation extends JPanel {

    private JButton excelRegiBtn;
    private JButton apiInfoRegiBtn;
    private JButton excelGenBtn;
    private JButton apiInfoDeleteModifyBtn;

    private final ComponentManager componentManager;

    public Navigation(Context context) {
        this.componentManager = context.componentManager();

        setting();
        setButton();
        setEvent();
    }

    private void setting() {
        this.setLayout(new FlowLayout());
        this.setBackground(Color.LIGHT_GRAY);
    }

    private void setButton() {
        excelRegiBtn = new JButton("엑셀 파일 등록");
        this.add(excelRegiBtn);

        apiInfoRegiBtn = new JButton("api 정보 등록");
        this.add(apiInfoRegiBtn);

        excelGenBtn = new JButton("엑셀 파일 생성");
        this.add(excelGenBtn);

        apiInfoDeleteModifyBtn = new JButton("api 정보 관리");
        this.add(apiInfoDeleteModifyBtn);
    }

    private void setEvent() {
        excelRegiBtn.addActionListener((e -> componentManager.getAll().forEach((key, panel) -> panel.setVisible(key.equals("excelRegister")))));
        apiInfoRegiBtn.addActionListener((e -> componentManager.getAll().forEach((key, panel) -> panel.setVisible(key.equals("apiInfoRegister")))));
        excelGenBtn.addActionListener((e -> componentManager.getAll().forEach((key, panel) -> panel.setVisible(key.equals("excelGen")))));
        apiInfoDeleteModifyBtn.addActionListener((e -> componentManager.getAll().forEach((key, panel) -> panel.setVisible(key.equals("apiInfoDeleteModify")))));
    }
}
