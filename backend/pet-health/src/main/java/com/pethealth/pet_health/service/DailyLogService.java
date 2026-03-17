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
        return repository.save(log);
    }

    public List<DailyLog> getLogs(Long petId, String type) {
        return repository.findByPetIdAndType(petId, type);
    }

    public void deleteLog(Long id) {
        repository.deleteById(id);
    }
}