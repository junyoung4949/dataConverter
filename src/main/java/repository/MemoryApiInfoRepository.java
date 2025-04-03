package repository;

import entity.ApiInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryApiInfoRepository implements ApiInfoRepository {

    private final Map<Long, ApiInfo> store = new HashMap<>();
    private final AtomicLong index = new AtomicLong(1L); // ID 증가용

    @Override
    public Long save(ApiInfo entity) {
        Long id = index.getAndIncrement(); // ID 자동 증가
        entity.setId(id);
        store.put(id, entity);
        return id;
    }

    @Override
    public ApiInfo get(Long id) {
        return store.get(id);
    }

    @Override
    public List<ApiInfo> getAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void update(Long id, ApiInfo apiInfo) {
        if (store.containsKey(id)) {
            apiInfo.setId(id);
            store.put(id, apiInfo);
        }
    }

    @Override
    public void remove(Long id) {
        store.remove(id);
    }
}