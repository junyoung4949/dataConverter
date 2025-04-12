package client.panel;

import dto.ApiInfoDto;
import entity.ApiInfo;
import repository.ApiInfoRepository;
import util.Context;
import util.Reloader;

import javax.swing.*;
import java.awt.*;

public class ApiInfoRegister extends JPanel {
    private JTextField nameTextFiled;
    private JTextField customerIdTextFiled;
    private JTextField accessLicenseTextFiled;
    private JTextField secretKeyTextFiled;
    private JButton submitButton;

    private final ApiInfoRepository apiInfoRepository;
    private final Reloader reloader;


    public ApiInfoRegister(Context context) {
        this.apiInfoRepository = context.apiInfoRepository();
        this.reloader = context.reloader();

        GridBagConstraints gbc = setting();

        // 입력 필드 추가
        addLabelAndField(gbc);

        // 제출 버튼 추가
        setSubmitButton(gbc);
        setButtonEvent();
    }

    private GridBagConstraints setting() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // 상하좌우 여백 설정
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void addLabelAndField(GridBagConstraints gbc) {
        gbc.gridx = 0; // 왼쪽 정렬
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        add(new JLabel("이름 :"), gbc);
        gbc.gridx = 1; // 오른쪽 입력 필드
        gbc.weightx = 0.7;
        this.nameTextFiled = new JTextField(20);
        add(nameTextFiled, gbc);

        gbc.gridx = 0; // 왼쪽 정렬
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        add(new JLabel("CUSTOMER_ID :"), gbc);
        gbc.gridx = 1; // 오른쪽 입력 필드
        gbc.weightx = 0.7;
        this.customerIdTextFiled = new JTextField(20);
        add(customerIdTextFiled, gbc);

        gbc.gridx = 0; // 왼쪽 정렬
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        add(new JLabel("엑세스 라이선스 :"), gbc);
        gbc.gridx = 1; // 오른쪽 입력 필드
        gbc.weightx = 0.7;
        this.accessLicenseTextFiled = new JTextField(20);
        add(accessLicenseTextFiled, gbc);

        gbc.gridx = 0; // 왼쪽 정렬
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        add(new JLabel("비밀키 :"), gbc);
        gbc.gridx = 1; // 오른쪽 입력 필드
        gbc.weightx = 0.7;
        this.secretKeyTextFiled = new JTextField(20);
        add(secretKeyTextFiled, gbc);
    }

    private void setSubmitButton(GridBagConstraints gbc) {
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        submitButton = new JButton("제출");
        add(submitButton, gbc);
    }

    private void setButtonEvent() {
        submitButton.addActionListener(e -> {
            ApiInfoDto dto = new ApiInfoDto(
                    nameTextFiled.getText(),
                    Long.parseLong(customerIdTextFiled.getText()),
                    accessLicenseTextFiled.getText(),
                    secretKeyTextFiled.getText()
            );
            apiInfoRepository.save(
                    new ApiInfo(dto.getName(), dto.getCustomerId(), dto.getAccessLicense(), dto.getSecretKey()));
            // Reloadable 구현체 모두 reload
            reloader.reload();

            // textFiled 모두 비우기
            setTextFiledEmpty();
        });
    }

    private void setTextFiledEmpty() {
        nameTextFiled.setText("");
        customerIdTextFiled.setText("");
        accessLicenseTextFiled.setText("");
        secretKeyTextFiled.setText("");
    }

}