package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    List<DailyLog> findByPetIdAndType(Long petId, String type);

    // Used by feeding reminder: check if any feeding log exists after a given time
    List<DailyLog> findByPetIdAndTypeAndCreateTimeAfter(Long petId, String type, LocalDateTime after);
}