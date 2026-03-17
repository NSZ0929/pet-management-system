package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.DailyLog;
import com.pethealth.pet_health.service.DailyLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/daily-logs")
public class DailyLogController {

    private final DailyLogService service;

    public DailyLogController(DailyLogService service) {
        this.service = service;
    }

    @PostMapping
    public DailyLog add(@RequestBody DailyLog log) {
        return service.addLog(log);
    }

    @GetMapping
    public List<DailyLog> get(
            @RequestParam Long petId,
            @RequestParam String type
    ) {
        return service.getLogs(petId, type);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteLog(id);
    }
}