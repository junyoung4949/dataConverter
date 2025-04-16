package client.panel;

import com.fasterxml.jackson.core.JsonProcessingException;
import dto.ExcelColumnDto;
import entity.ApiInfo;
import repository.ApiInfoRepository;
import service.ExcelDataService;
import service.ExcelEditService;
import util.Context;
import util.Reloadable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private Set<ApiInfo> selectedItems = new HashSet<>(); // 선택된 항목 저장
    private File selectedFolder;
    private JTextField dateTextFiled;

    private final ApiInfoRepository apiInfoRepository;
    private final ExcelDataService excelDataService;
    private final ExcelEditService excelEditService;

    public ExcelGen(Context context) {
        this.apiInfoRepository = context.apiInfoRepository();
        this.excelDataService = context.excelDataService();
        this.excelEditService = context.excelEditService();

        setLayout(new BorderLayout());

        // 왼쪽 패널 (API 리스트)
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
                        selectedItems.remove(item); // 이미 선택된 경우 해제
                    } else {
                        selectedItems.add(item); // 새로 선택
                    }
                    apiList.repaint(); // UI 갱신
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(apiList);
        reload(); // 초기 데이터 로드

        // 엑셀 생성 버튼, 날짜 입력 필드, 디렉토리 선택 버튼
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());

        // 날짜 입력 필드
        dateTextFiled = new JTextField(25);

        // 디렉토리 선택 버튼
        JButton directoryChooseBtn = new JButton("폴더 선택");
        this.fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // 폴더만 선택 가능
        directoryChooseBtn.addActionListener(this::selectDirectory);

        // 엑셀 생성 버튼
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

            Map<String, List<ExcelColumnDto>> excelData = excelDataService.getExcelData(new ArrayList<>(selectedItems), dateRange);
            excelEditService.editAndSave(excelData, selectedFolder);
        });

        // southPanel에 엑셀 생성 버튼, 날짜 입력 필드, 디렉토리 선택 버튼 등록
        southPanel.add(directoryChooseBtn, BorderLayout.CENTER);
        southPanel.add(excelGenBtn, BorderLayout.EAST);
        southPanel.add(dateTextFiled, BorderLayout.WEST);


        // 레이아웃 구성
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private boolean isInvalidDateFormat() {
        return !(dateTextFiled.getText().matches("\\d{8}~\\d{8}") || dateTextFiled.getText().matches("\\d{8}"));
    }

    @Override
    public void reload() {
        listModel.clear();
        selectedItems.clear(); // 갱신 시 선택 항목 초기화
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
            setSelected(selectedItems.contains(value)); // 선택 상태 반영
            return this;
        }
    }
}
