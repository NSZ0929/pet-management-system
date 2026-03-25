package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.DailyLog;
import com.pethealth.pet_health.repository.DailyLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyLogService {

    private final DailyLogRepository repository;

    public DailyLogService(DailyLogRepository repository) {
        this.repository = repository;
    }

    public DailyLog addLog(DailyLog log) {
        if (log == null) throw new IllegalArgumentException("Daily log cannot be null");
        if (log.getPetId() == null) throw new IllegalArgumentException("Pet ID cannot be null");
        if (log.getType() == null || log.getType().isBlank()) {
            throw new IllegalArgumentException("Log type cannot be blank");
        }
        if (log.getContent() == null || log.getContent().isBlank()) {
            throw new IllegalArgumentException("Log content cannot be blank");
        }
        return repository.save(log);
    }

    public List<DailyLog> getLogs(Long petId, String type) {
        if (petId == null) throw new IllegalArgumentException("Pet ID cannot be null");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Log type cannot be blank");
        return repository.findByPetIdAndType(petId, type);
    }

    public void deleteLog(Long id) {
        if (id == null) throw new IllegalArgumentException("Log ID cannot be null");
        repository.deleteById(id);
    }
}