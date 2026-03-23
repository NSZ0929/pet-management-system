package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.MedicalRecord;
import com.pethealth.pet_health.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // Find by pet ID
    List<MedicalRecord> findByPetId(@NonNull Long petId);

    // Find by pet entity (used by notification schedulers)
    List<MedicalRecord> findByPet(@NonNull Pet pet);

    // Fuzzy search by title
    List<MedicalRecord> findByTitleContainingIgnoreCase(@NonNull String title);
}