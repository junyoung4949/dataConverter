package repository;

import entity.Excel;
import java.sql.*;

public class DBExcelRepository implements ExcelRepository {

    private static final String DB_URL = "jdbc:sqlite:database.db"; // DB 파일

    public DBExcelRepository() {
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS excel (id INTEGER PRIMARY KEY AUTOINCREMENT, location TEXT, password TEXT)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Excel entity) {
        String deleteSql = "DELETE FROM excel";
        String updateSql = "INSERT INTO excel (location, password) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
            PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {

            updatePstmt.setString(1, entity.getLocation());
            updatePstmt.setString(2, entity.getPassword());

            deletePstmt.executeUpdate(); // 기존 데이터 삭제
            updatePstmt.executeUpdate(); // 새로운 데이터 입력
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Excel get() {
        String sql = "SELECT * FROM excel LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return new Excel(
                        rs.getString("location"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(Excel entity) {
        String deleteSql = "DELETE FROM excel";
        String updateSql = "INSERT INTO excel (location, password) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
             PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {

            updatePstmt.setString(1, entity.getLocation());
            updatePstmt.setString(2, entity.getPassword());

            deletePstmt.executeUpdate(); // 기존 데이터 삭제
            updatePstmt.executeUpdate(); // 새로운 데이터 입력
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete() {
        String sql = "DELETE FROM excel";

        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

