package repository;

import entity.Excel;

public class MemoryExcelRepository implements ExcelRepository {

    private Excel entity;

    @Override
    public void save(Excel entity) {
        this.entity = entity;
    }

    @Override
    public Excel get() {
        return entity;
    }

    @Override
    public void update(Excel entity) {
        this.entity = entity;
    }

    @Override
    public void delete() {
        this.entity = null;
    }
}
