package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // 根据宠物的 id 查询病历
    List<MedicalRecord> findByPetId(@NonNull Long petId);

    // 根据病历标题模糊查询
    List<MedicalRecord> findByTitleContainingIgnoreCase(@NonNull String title);
}
