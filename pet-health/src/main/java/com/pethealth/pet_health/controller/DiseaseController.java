package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.Disease;
import com.pethealth.pet_health.service.DiseaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/diseases")
public class DiseaseController {

    private final DiseaseService diseaseService;

    public DiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    @GetMapping
    public List<Disease> getAllDiseases() {
        return diseaseService.getAllDiseases();
    }

    @PostMapping
    public Disease addDisease(@RequestBody Disease disease) {
        Objects.requireNonNull(disease, "Disease must not be null");
        return diseaseService.addDisease(disease);
    }

    @GetMapping("/{id}")
    public Disease getDiseaseById(@PathVariable Long id) {
        Objects.requireNonNull(id, "Id must not be null");
        return diseaseService.getDiseaseById(id);
    }

    @PutMapping("/{id}")
    public Disease updateDisease(
            @PathVariable Long id,
            @RequestBody Disease updatedDisease
    ) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(updatedDisease, "Updated Disease must not be null");
        return diseaseService.updateDisease(id, updatedDisease);
    }

    @DeleteMapping("/{id}")
    public void deleteDisease(@PathVariable Long id) {
        Objects.requireNonNull(id, "Id must not be null");
        diseaseService.deleteDisease(id);
    }

    @GetMapping("/search")
    public List<Disease> searchDiseasesByName(@RequestParam String name) {
        Objects.requireNonNull(name, "Search name must not be null");
        return diseaseService.searchDiseasesByName(name);
    }
}
