package repository;

import entity.Excel;

import java.util.List;

public interface ExcelRepository {
    void save(Excel entity);
    Excel get();
    void update(Excel excel);
}
