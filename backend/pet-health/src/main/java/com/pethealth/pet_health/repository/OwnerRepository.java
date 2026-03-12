package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * OwnerRepository - 主人数据访问接口
 */
@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    /**
     * 根据姓名模糊搜索主人
     */
    List<Owner> findByNameContainingIgnoreCase(@NonNull String name);
}
