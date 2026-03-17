package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    List<DailyLog> findByPetIdAndType(Long petId, String type);
}