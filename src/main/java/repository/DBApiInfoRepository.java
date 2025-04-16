package repository;

import entity.ApiInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBApiInfoRepository implements ApiInfoRepository {

    private static final String DB_URL = "jdbc:sqlite:database.db"; // DB 파일

    public DBApiInfoRepository() {
        createTable(); // 테이블 자동 생성
    }

    // 테이블 생성
    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS api_information (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, customId INTEGER, accessLicense TEXT, secretKey TEXT)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
             stmt.execute(sql);
        } catch (SQLException e) {
             e.printStackTrace();
        }
    }

    @Override
    public Long save(ApiInfo entity) {
        String sql = "INSERT INTO api_information (name, customId, accessLicense, secretKey) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, entity.getName());
            pstmt.setLong(2, entity.getCustomerId());
            pstmt.setString(3, entity.getAccessLicense());
            pstmt.setString(4, entity.getSecretKey());
            pstmt.executeUpdate();

            // 자동 생성된 키 가져오기
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1); // 생성된 ID 반환
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1L; // 실패 시 -1 반환
    }

    @Override
    public ApiInfo get(Long id) {
        String sql = "SELECT * FROM api_information WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ApiInfo(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getLong("customId"),
                            rs.getString("accessLicense"),
                            rs.getString("secretKey")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 조회 실패 시 null 반환
    }

    @Override
    public List<ApiInfo> getAll() {
        List<ApiInfo> apiInfoList = new ArrayList<>();
        String sql = "SELECT * FROM api_information";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                apiInfoList.add(new ApiInfo(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getLong("customId"),
                        rs.getString("accessLicense"),
                        rs.getString("secretKey")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apiInfoList;
    }

    @Override
    public void update(Long id, ApiInfo apiInfo) {
        String sql = "UPDATE api_information SET name = ?, customId = ?, accessLicense = ?, secretKey = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, apiInfo.getName());
            pstmt.setLong(2, apiInfo.getCustomerId());
            pstmt.setString(3, apiInfo.getAccessLicense());
            pstmt.setString(4, apiInfo.getSecretKey());
            pstmt.setLong(5, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void remove(Long id) {
        String sql = "DELETE FROM api_information WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

