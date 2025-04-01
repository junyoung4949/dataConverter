package repository;

import entity.Excel;
import java.sql.*;

public class DBExcelRepository implements ExcelRepository {
    @Override
    public void save(Excel entity) {
    }

    @Override
    public Excel get() {
        return null;
    }

    @Override
    public void update(Excel excel) {
    }

//    private static final String URL = "jdbc:h2:~/test"; // H2 사용 예제 (MySQL 사용 시 변경 가능)
//    private static final String USER = "sa"; // MySQL 사용 시 "root" 또는 설정된 사용자
//    private static final String PASSWORD = "";
//
//    public DBExcelRepository() {
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             Statement stmt = conn.createStatement()) {
//            String sql = "CREATE TABLE IF NOT EXISTS excel (" +
//                    "location VARCHAR(255), " +
//                    "password VARCHAR(255))";
//            stmt.executeUpdate(sql);
//        } catch (SQLException e) {
//            throw new RuntimeException("테이블 생성 실패", e);
//        }
//    }
//
//    @Override
//    public void save(Excel entity) {
//        // 기존 데이터 삭제 후 새로운 데이터 저장
//        String deleteSql = "DELETE FROM excel";
//        String insertSql = "INSERT INTO excel (location, password) VALUES (?, ?)";
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
//             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
//
//            deleteStmt.executeUpdate(); // 기존 데이터 삭제
//
//            insertStmt.setString(1, entity.getLocation());
//            insertStmt.setString(2, entity.getPassword());
//            insertStmt.executeUpdate();
//
//        } catch (SQLException e) {
//            throw new RuntimeException("저장 실패", e);
//        }
//    }
//
//    @Override
//    public Excel get() {
//        String sql = "SELECT * FROM excel LIMIT 1"; // 하나만 존재하므로 LIMIT 1 추가
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(sql);
//             ResultSet rs = pstmt.executeQuery()) {
//
//            if (rs.next()) {
//                return new Excel(
//                        rs.getString("location"),
//                        rs.getString("password")
//                );
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("조회 실패", e);
//        }
//        return null;
//    }
//
//    @Override
//    public void update(Excel excel) {
//        String sql = "UPDATE excel SET location = ?, password = ?";
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, excel.getLocation());
//            pstmt.setString(2, excel.getPassword());
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            throw new RuntimeException("업데이트 실패", e);
//        }
//    }
}

