package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.VitalSign;
import com.pethealth.pet_health.repository.VitalSignRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VitalSignService {

    private final VitalSignRepository repository;

    public VitalSignService(VitalSignRepository repository) {
        this.repository = repository;
    }

    public VitalSign add(VitalSign vs) {
        return repository.save(vs);
    }

    public List<VitalSign> getByPet(Long petId) {
        return repository.findByPetId(petId);
    }
}