package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.MedicalRecord;
import com.pethealth.pet_health.entity.Pet;
import com.pethealth.pet_health.entity.Disease;
import com.pethealth.pet_health.repository.MedicalRecordRepository;
import com.pethealth.pet_health.repository.PetRepository;
import com.pethealth.pet_health.repository.DiseaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PetRepository petRepository;
    private final DiseaseRepository diseaseRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PetRepository petRepository,
                                DiseaseRepository diseaseRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.petRepository = petRepository;
        this.diseaseRepository = diseaseRepository;
    }

    public List<MedicalRecord> getAllRecords() {
        return medicalRecordRepository.findAll();
    }

    public MedicalRecord getRecordById(Long id) {
        if (id == null) throw new IllegalArgumentException("Record ID cannot be null");
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with id: " + id));
    }

    /**
     * 添加医疗记录，同时可传 diseaseId 关联疾病
     */
    public MedicalRecord addRecord(MedicalRecord record, Long petId, Long diseaseId) {
        if (record == null) throw new IllegalArgumentException("MedicalRecord cannot be null");
        if (petId == null) throw new IllegalArgumentException("Pet ID cannot be null");

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + petId));
        record.setPet(pet);

        if (diseaseId != null) {
            Disease disease = diseaseRepository.findById(diseaseId)
                    .orElseThrow(() -> new RuntimeException("Disease not found with id: " + diseaseId));
            record.setDisease(disease);
        }

        if (record.getVisitDate() == null) {
            record.setVisitDate(LocalDate.now());
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * 更新医疗记录，可更新 diseaseId
     */
    public MedicalRecord updateRecord(Long id, MedicalRecord updatedRecord, Long diseaseId) {
        if (id == null) throw new IllegalArgumentException("Record ID cannot be null");
        if (updatedRecord == null) throw new IllegalArgumentException("Updated record cannot be null");

        MedicalRecord record = getRecordById(id);

        if (updatedRecord.getTitle() != null) {
            record.setTitle(updatedRecord.getTitle());
        }
        if (updatedRecord.getDescription() != null) {
            record.setDescription(updatedRecord.getDescription());
        }
        if (updatedRecord.getVisitDate() != null) {
            record.setVisitDate(updatedRecord.getVisitDate());
        }

        // 只有在前端明确传了 diseaseId 时才更新疾病关联；
        // 未传时保留原有 disease，避免编辑标题/描述时把原关联清空。
        if (diseaseId != null) {
            Disease disease = diseaseRepository.findById(diseaseId)
                    .orElseThrow(() -> new RuntimeException("Disease not found with id: " + diseaseId));
            record.setDisease(disease);
        }

        return medicalRecordRepository.save(record);
    }

    public void deleteRecord(Long id) {
        if (id == null) throw new IllegalArgumentException("Record ID cannot be null");
        if (!medicalRecordRepository.existsById(id)) {
            throw new RuntimeException("Record not found with id: " + id);
        }
        medicalRecordRepository.deleteById(id);
    }

    public List<MedicalRecord> getRecordsByPet(Long petId) {
        if (petId == null) throw new IllegalArgumentException("Pet ID cannot be null");
        return medicalRecordRepository.findByPetId(petId);
    }

    public List<MedicalRecord> searchRecords(Long petId, String keyword) {
        if (petId == null) throw new IllegalArgumentException("Pet ID cannot be null");
        if (keyword == null || keyword.trim().isEmpty()) {
            return getRecordsByPet(petId);
        }
        return medicalRecordRepository.searchByPetAndKeyword(petId, keyword.trim());
    }
}
