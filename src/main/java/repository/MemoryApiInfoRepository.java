package repository;

import entity.ApiInfo;

import java.util.ArrayList;
import java.util.List;

public class MemoryApiInfoRepository implements ApiInfoRepository {

    private final static List<ApiInfo> store = new ArrayList<>();
    private static Long index = 0L;

    @Override
    public Long save(ApiInfo entity) {
        entity.setId(index);
        store.add(index.intValue(), entity);
        return index++;
    }

    @Override
    public ApiInfo get(Long id) {
        return store.get(id.intValue());
    }

    @Override
    public List<ApiInfo> getAll() {
        return store;
    }

    @Override
    public void update(Long id, ApiInfo apiInfo) {
        store.add(id.intValue(), apiInfo);
    }

    @Override
    public void remove(Long id) {
        store.remove(id.intValue());
    }
}
