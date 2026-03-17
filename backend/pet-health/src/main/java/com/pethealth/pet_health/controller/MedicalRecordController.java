package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.MedicalRecord;
import com.pethealth.pet_health.service.MedicalRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping
    public List<MedicalRecord> getAllRecords() {
        return medicalRecordService.getAllRecords();
    }

    /**
     * 添加医疗记录，同时可以传 diseaseId 关联疾病
     */
    @PostMapping
    public MedicalRecord addRecord(
            @RequestBody MedicalRecord record,
            @RequestParam Long petId,
            @RequestParam(required = false) Long diseaseId
    ) {
        if (petId == null) {
            throw new RuntimeException("Pet ID must not be null");
        }
        return medicalRecordService.addRecord(record, petId, diseaseId);
    }

    @GetMapping("/{id}")
    public MedicalRecord getRecord(@PathVariable Long id) {
        return medicalRecordService.getRecordById(id);
    }

    /**
     * 更新医疗记录，同时可以更新 diseaseId
     */
    @PutMapping("/{id}")
    public MedicalRecord updateRecord(
            @PathVariable Long id,
            @RequestBody MedicalRecord updatedRecord,
            @RequestParam(required = false) Long diseaseId
    ) {
        return medicalRecordService.updateRecord(id, updatedRecord, diseaseId);
    }

    @DeleteMapping("/{id}")
    public void deleteRecord(@PathVariable Long id) {
        medicalRecordService.deleteRecord(id);
    }

    @GetMapping("/pet/{petId}")
    public List<MedicalRecord> getRecordsByPet(@PathVariable Long petId) {
        return medicalRecordService.getRecordsByPet(petId);
    }

    @GetMapping("/search")
    public List<MedicalRecord> searchRecords(@RequestParam String title) {
        return medicalRecordService.searchRecords(title);
    }
}
