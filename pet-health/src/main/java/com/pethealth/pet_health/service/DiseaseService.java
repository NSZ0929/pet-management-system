package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.Disease;
import com.pethealth.pet_health.repository.DiseaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    public DiseaseService(DiseaseRepository diseaseRepository) {
        this.diseaseRepository = diseaseRepository;
    }

    /**
     * 获取所有疾病
     */
    public List<Disease> getAllDiseases() {
        return diseaseRepository.findAll();
    }

    /**
     * 添加新疾病
     */
    public Disease addDisease(Disease disease) {
        Objects.requireNonNull(disease, "Disease must not be null");
        return diseaseRepository.save(disease);
    }

    /**
     * 根据 ID 获取疾病
     */
    public Disease getDiseaseById(Long id) {
        Objects.requireNonNull(id, "Id must not be null");
        return diseaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Disease not found with id: " + id));
    }

    /**
     * 更新疾病信息
     */
    public Disease updateDisease(Long id, Disease updatedDisease) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(updatedDisease, "Updated Disease must not be null");

        Disease disease = getDiseaseById(id); // 已包含不存在异常

        disease.setName(updatedDisease.getName());
        disease.setDescription(updatedDisease.getDescription());

        return diseaseRepository.save(disease);
    }

    /**
     * 删除疾病
     */
    public void deleteDisease(Long id) {
        Objects.requireNonNull(id, "Id must not be null");
        if (!diseaseRepository.existsById(id)) {
            throw new EntityNotFoundException("Disease not found with id: " + id);
        }
        diseaseRepository.deleteById(id);
    }

    /**
     * 根据名称模糊搜索
     */
    public List<Disease> searchDiseasesByName(String name) {
        Objects.requireNonNull(name, "Search name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Search name cannot be empty");
        }
        return diseaseRepository.findByNameContainingIgnoreCase(name);
    }
}
