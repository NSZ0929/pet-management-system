package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.Vet;
import com.pethealth.pet_health.repository.VetRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VetService {

    private final VetRepository vetRepository;

    public VetService(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    // 新增兽医
    public Vet addVet(@NonNull Vet vet) {
        return vetRepository.save(vet);
    }

    // 根据 ID 获取兽医
    public Vet getVetById(@NonNull Long id) {
        return vetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vet not found with id: " + id));
    }

    // 获取所有兽医
    public List<Vet> getAllVets() {
        return vetRepository.findAll();
    }

    // 删除兽医
    public void deleteVet(@NonNull Long id) {
        if (!vetRepository.existsById(id)) {
            throw new RuntimeException("Vet not found with id: " + id);
        }
        vetRepository.deleteById(id);
    }
}
