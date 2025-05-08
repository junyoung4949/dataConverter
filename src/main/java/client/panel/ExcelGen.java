package client.panel;

import client.worker.ExcelGenerateWorkerExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;
import dto.ExcelColumnDto;
import entity.ApiInfo;
import repository.ApiInfoRepository;
import service.ExcelDataService;
import service.ExcelEditService;
import util.Context;
import util.Reloadable;
import client.worker.ExcelGenerateWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class ExcelGen extends JPanel implements Reloadable {
    private DefaultListModel<ApiInfo> listModel;
    private JList<ApiInfo> apiList;
    private JButton excelGenBtn;
    private JFileChooser fileChooser;
    private Set<ApiInfo> selectedItems = new HashSet<>();
    private File selectedFolder;
    private JTextField dateTextFiled;
    private JProgressBar progressBar;

    private final ApiInfoRepository apiInfoRepository;
    private final ExcelDataService excelDataService;
    private final ExcelEditService excelEditService;
    private final ExcelGenerateWorkerExecutor excelGenerateWorkerExecutor;

    public ExcelGen(Context context) {
        this.apiInfoRepository = context.apiInfoRepository();
        this.excelDataService = context.excelDataService();
        this.excelEditService = context.excelEditService();
        this.excelGenerateWorkerExecutor = context.excelGenerateWorkerExecutor();

        setLayout(new BorderLayout());

        // 왼쪽 리스트
        listModel = new DefaultListModel<>();
        apiList = new JList<>(listModel);
        apiList.setCellRenderer(new CheckBoxListRenderer());
        apiList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = apiList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    ApiInfo item = listModel.get(index);
                    if (selectedItems.contains(item)) {
                        selectedItems.remove(item);
                    } else {
                        selectedItems.add(item);
                    }
                    apiList.repaint();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(apiList);
        reload();

        // 하단 패널 구성
        JPanel southPanel = new JPanel(new BorderLayout());
        dateTextFiled = new JTextField(25);
        JButton directoryChooseBtn = new JButton("폴더 선택");

        this.fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooseBtn.addActionListener(this::selectDirectory);

        excelGenBtn = new JButton("엑셀 생성");
        excelGenBtn.addActionListener(e -> {
            String dateRange = dateTextFiled.getText();

            if (selectedItems.isEmpty()) {
                JOptionPane.showMessageDialog(null, "생성할 항목을 선택해주세요");
                return;
            }

            if (selectedFolder == null) {
                JOptionPane.showMessageDialog(null, "생성할 위치를 지정해주세요");
                return;
            }

            if (isInvalidDateFormat()) {
                JOptionPane.showMessageDialog(null, "날짜 포맷은 : \"20250303~20250305\" 또는 \"20250303\" 과 같이 입력해주세요.");
                return;
            }

            // ExcelGenerateWorker 실행
            excelGenerateWorkerExecutor.excelGenExecute(selectedItems, dateRange, selectedFolder, excelGenBtn, progressBar, this);
        });

        southPanel.add(directoryChooseBtn, BorderLayout.CENTER);
        southPanel.add(excelGenBtn, BorderLayout.EAST);
        southPanel.add(dateTextFiled, BorderLayout.WEST);

        // 상단에 진행바 추가
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.NORTH);

        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private boolean isInvalidDateFormat() {
        return !(dateTextFiled.getText().matches("\\d{8}~\\d{8}") || dateTextFiled.getText().matches("\\d{8}"));
    }

    @Override
    public void reload() {
        listModel.clear();
        selectedItems.clear();
        List<ApiInfo> apiInfos = apiInfoRepository.getAll();
        apiInfos.forEach(listModel::addElement);
    }

    private void selectDirectory(ActionEvent e) {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            this.selectedFolder = fileChooser.getSelectedFile();
        }
    }

    private class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer<ApiInfo> {
        @Override
        public Component getListCellRendererComponent(JList<? extends ApiInfo> list, ApiInfo value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.getName());
            setSelected(selectedItems.contains(value));
            return this;
        }
    }
}