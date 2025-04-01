package repository;

import entity.ApiInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBApiInfoRepository implements ApiInfoRepository {
    @Override
    public Long save(ApiInfo entity) {
        return null;
    }

    @Override
    public ApiInfo get(Long id) {
        return null;
    }

    @Override
    public List<ApiInfo> getAll() {
        return null;
    }

    @Override
    public void update(Long id, ApiInfo apiInfo) {

    }

    @Override
    public void remove(Long id) {

    }

//    private static final String URL = "jdbc:h2:~/test"; // H2 사용 예제 (MySQL 사용 시 변경 가능)
//    private static final String USER = "sa"; // MySQL 사용 시 "root" 또는 설정된 사용자
//    private static final String PASSWORD = "";
//
//    public DBApiInfoRepository() {
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             Statement stmt = conn.createStatement()) {
//            String sql = "CREATE TABLE IF NOT EXISTS api_info (" +
//                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
//                    "name VARCHAR(255), " +
//                    "customerId VARCHAR(255), " +
//                    "accessLicense VARCHAR(255), " +
//                    "secretKey VARCHAR(255))";
//            stmt.executeUpdate(sql);
//        } catch (SQLException e) {
//            throw new RuntimeException("테이블 생성 실패", e);
//        }
//    }
//
//    @Override
//    public Long save(ApiInfo entity) {
//        String sql = "INSERT INTO api_info (name, customerId, accessLicense, secretKey) VALUES (?, ?, ?, ?)";
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//
//            pstmt.setString(1, entity.getName());
//            pstmt.setString(2, entity.getCustomerId());
//            pstmt.setString(3, entity.getAccessLicense());
//            pstmt.setString(4, entity.getSecretKey());
//            pstmt.executeUpdate();
//
//            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
//                if (generatedKeys.next()) {
//                    return generatedKeys.getLong(1);
//                }
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("저장 실패", e);
//        }
//        return null;
//    }
//
//    @Override
//    public ApiInfo get(Long id) {
//        String sql = "SELECT * FROM api_info WHERE id = ?";
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setLong(1, id);
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//                    return new ApiInfo(
//                            rs.getLong("id"),
//                            rs.getString("name"),
//                            rs.getString("customerId"),
//                            rs.getString("accessLicense"),
//                            rs.getString("secretKey")
//                    );
//                }
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("조회 실패", e);
//        }
//        return null;
//    }
//
//    @Override
//    public List<ApiInfo> getAll() {
//        List<ApiInfo> apiInfos = new ArrayList<>();
//        String sql = "SELECT * FROM api_info";
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(sql);
//             ResultSet rs = pstmt.executeQuery()) {
//
//            while (rs.next()) {
//                apiInfos.add(new ApiInfo(
//                        rs.getLong("id"),
//                        rs.getString("name"),
//                        rs.getString("customerId"),
//                        rs.getString("accessLicense"),
//                        rs.getString("secretKey")
//                ));
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("전체 조회 실패", e);
//        }
//        return apiInfos;
//    }
//
//    @Override
//    public void update(Long id, ApiInfo apiInfo) {
//        String sql = "UPDATE api_info SET name = ?, customerId = ?, accessLicense = ?, secretKey = ? WHERE id = ?";
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, apiInfo.getName());
//            pstmt.setString(2, apiInfo.getCustomerId());
//            pstmt.setString(3, apiInfo.getAccessLicense());
//            pstmt.setString(4, apiInfo.getSecretKey());
//            pstmt.setLong(5, id);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            throw new RuntimeException("업데이트 실패", e);
//        }
//    }
}

