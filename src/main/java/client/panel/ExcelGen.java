package client.panel;

import entity.ApiInfo;
import repository.ApiInfoRepository;
import util.Context;
import util.Reloadable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExcelGen extends JPanel implements Reloadable {
    private DefaultListModel<ApiInfo> listModel;
    private JList<ApiInfo> apiList;
    private JButton excelGenBtn;
    private Set<ApiInfo> selectedItems = new HashSet<>(); // 선택된 항목 저장

    private final ApiInfoRepository apiInfoRepository;

    public ExcelGen(Context context) {
        this.apiInfoRepository = context.apiInfoRepository();

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

        // 엑셀 생성 버튼
        excelGenBtn = new JButton("엑셀 생성");
        excelGenBtn.addActionListener(e -> {
            if (selectedItems.isEmpty()) {
                JOptionPane.showMessageDialog(null, "삭제할 항목을 선택하세요.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null, "선택한 항목을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                selectedItems.forEach(apiInfo -> apiInfoRepository.remove(apiInfo.getId())); // 삭제
                selectedItems.clear(); // 선택 목록 초기화
                reload(); // 삭제 후 리스트 갱신
            }
        });

        // 레이아웃 구성
        add(scrollPane, BorderLayout.CENTER);
        add(excelGenBtn, BorderLayout.SOUTH);
    }

    @Override
    public void reload() {
        listModel.clear();
        selectedItems.clear(); // 갱신 시 선택 항목 초기화
        List<ApiInfo> apiInfos = apiInfoRepository.getAll();
        apiInfos.forEach(listModel::addElement);
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
