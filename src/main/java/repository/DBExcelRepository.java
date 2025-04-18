package repository;

import entity.Excel;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DBExcelRepository implements ExcelRepository {

    private static final String DB_FILE_PATH = "database/database.db"; // 외부 DB 파일
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE_PATH;

    public DBExcelRepository() {
        ensureDatabaseExists(); // DB 파일 복사
        createTable();
    }

    // DB가 없으면 resources의 init_database.db에서 복사
    private void ensureDatabaseExists() {
        Path dbPath = Paths.get(DB_FILE_PATH);
        if (!Files.exists(dbPath)) {
            try (InputStream in = getClass().getResourceAsStream("/init_database.db")) {
                if (in == null) {
                    throw new RuntimeException("init_database.db 리소스를 찾을 수 없습니다.");
                }
                Files.createDirectories(dbPath.getParent());
                Files.copy(in, dbPath);
                System.out.println("초기 init_database.db 파일을 복사했습니다.");
            } catch (Exception e) {
                throw new RuntimeException("init_database.db 파일 복사 실패", e);
            }
        }
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

            deletePstmt.executeUpdate();
            updatePstmt.executeUpdate();
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
        save(entity); // save가 delete + insert니까 재사용 가능
    }

    @Override
    public void delete() {
        String sql = "DELETE FROM excel";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}