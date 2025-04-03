package client.panel;

import entity.Excel;
import repository.ExcelRepository;
import util.Context;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.awt.event.ActionEvent;

public class ExcelRegister extends JPanel {
    private JLabel fileNameLabel;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JFileChooser fileChooser;
    private JButton fileSelectBtn;
    private JButton saveBtn;
    private JButton deleteBtn;
    private File selectedFile;
    private GridBagConstraints gbc;

    private final ExcelRepository excelRepository;

    public ExcelRegister(Context context) {
        this.excelRepository = context.excelRepository();

        setting();
        setComponent();
        arrange();
        setButtonEvent();

        loadData();
    }

    private void loadData() {
        Excel excel = excelRepository.get();
        if (excel == null) {
            return;
        }
        String location = excel.getLocation();
        String password = excel.getPassword();
        try {
            selectedFile = new File(location);
            passwordField.setText(password);

            fileNameLabel.setText(selectedFile.getName());

        } catch (NullPointerException e) {
            JOptionPane.showMessageDialog(this, "파일의 위치가 변경되었을 경우, 파일을 새로 등록해주세요", "알림", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setting() {
        setLayout(new GridBagLayout());
        this.gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 패딩 추가
    }

    private void setComponent() {
        passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);
        fileSelectBtn = new JButton("파일 선택");
        saveBtn = new JButton("저장");
        deleteBtn = new JButton("삭제");
        fileNameLabel = new JLabel("파일을 선택해주세요.");
        fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel 파일 (*.xlsx)", "xlsx");
        fileChooser.setFileFilter(filter);
    }

    private void setButtonEvent() {
        fileSelectBtn.addActionListener(this::selectFile);
        saveBtn.addActionListener(e -> {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "파일을 선택하세요.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String password = new String(passwordField.getPassword());
            excelRepository.save(new Excel(selectedFile.getPath(), password));
            JOptionPane.showMessageDialog(this, "파일 저장됨", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
        });
        deleteBtn.addActionListener(e -> {
            selectedFile = null;
            fileNameLabel.setText("파일을 선택해주세요.");
            passwordField.setText("");
            excelRepository.delete();
            JOptionPane.showMessageDialog(this, "파일 선택이 취소되었습니다.", "삭제 완료", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void selectFile(ActionEvent e) {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileNameLabel.setText(selectedFile.getName());
        }
    }

    private void arrange() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(fileSelectBtn, gbc);

        gbc.gridy = 0;
        gbc.gridx = 2;
        add(fileNameLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        gbc.gridx = 2;
        add(passwordField, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(saveBtn, gbc);

        gbc.gridx = 1;
        add(deleteBtn, gbc);
    }
}

