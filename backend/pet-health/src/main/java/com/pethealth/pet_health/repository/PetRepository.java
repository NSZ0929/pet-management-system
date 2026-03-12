package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * PetRepository - 宠物数据访问接口
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    /**
     * 查询某个主人名下的所有宠物
     */
    List<Pet> findByOwnerId(@NonNull Long ownerId);

    /**
     * 根据宠物名称模糊查询
     */
    List<Pet> findByNameContainingIgnoreCase(@NonNull String name);
}
