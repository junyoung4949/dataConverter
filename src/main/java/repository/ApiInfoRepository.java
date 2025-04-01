package repository;

import entity.ApiInfo;

import java.util.List;

public interface ApiInfoRepository {

    Long save(ApiInfo entity);
    ApiInfo get(Long id);
    List<ApiInfo> getAll();
    void update(Long id, ApiInfo apiInfo);
    void remove(Long id);
}
