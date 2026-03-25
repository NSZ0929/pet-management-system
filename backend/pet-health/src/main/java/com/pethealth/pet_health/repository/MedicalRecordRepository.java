package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.MedicalRecord;
import com.pethealth.pet_health.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT mr FROM MedicalRecord mr
            WHERE mr.pet.id = :petId
              AND (LOWER(mr.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(mr.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<MedicalRecord> searchByPetAndKeyword(@Param("petId") Long petId, @Param("keyword") String keyword);
}