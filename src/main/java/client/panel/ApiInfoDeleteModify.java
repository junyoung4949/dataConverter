package client.panel;

import entity.ApiInfo;
import repository.ApiInfoRepository;
import util.Context;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ApiInfoDeleteModify extends JPanel {
    private DefaultListModel<ApiInfo> listModel;
    private JList<ApiInfo> apiList;
    private JButton deleteButton;

    private final ApiInfoRepository apiInfoRepository;

    public ApiInfoDeleteModify(Context context) {
        this.apiInfoRepository = context.apiInfoRepository();
        setLayout(new BorderLayout());

        // 왼쪽 패널 (API 리스트)
        listModel = new DefaultListModel<>();
        apiList = new JList<>(listModel);
        apiList.setCellRenderer(new CheckBoxListRenderer());
        apiList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(apiList);
        reloadApiInfo(); // 초기 데이터 로드

        // 삭제 버튼
        deleteButton = new JButton("선택 삭제");
        deleteButton.addActionListener(e -> {
            List<ApiInfo> selectedItems = apiList.getSelectedValuesList();
            if (selectedItems.isEmpty()) {
                JOptionPane.showMessageDialog(null, "삭제할 항목을 선택하세요.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null, "선택한 항목을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                selectedItems.forEach(apiInfo -> apiInfoRepository.remove(apiInfo.getId())); // 삭제 로직 구현 필요
                reloadApiInfo(); // 삭제 후 리스트 갱신
            }
        });

        // 레이아웃 구성
        add(scrollPane, BorderLayout.CENTER);
        add(deleteButton, BorderLayout.SOUTH);
    }

    /**
     * 🔄 외부에서 호출할 수 있도록 리스트 갱신 메서드 추가
     */
    public void reloadApiInfo() {
        listModel.clear();
        List<ApiInfo> apiInfos = apiInfoRepository.getAll();
        apiInfos.forEach(listModel::addElement);
    }

    private static class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer<ApiInfo> {
        @Override
        public Component getListCellRendererComponent(JList<? extends ApiInfo> list, ApiInfo value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.getName());
            setSelected(isSelected);
            return this;
        }
    }
}
