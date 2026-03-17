package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.Vet;
import com.pethealth.pet_health.service.VetService;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vets")
public class VetController {

    private final VetService vetService;

    public VetController(VetService vetService) {
        this.vetService = vetService;
    }

    @PostMapping
    public Vet addVet(@RequestBody @NonNull Vet vet) {
        return vetService.addVet(vet);
    }

    @GetMapping
    public List<Vet> getAllVets() {
        return vetService.getAllVets();
    }

    @GetMapping("/{id}")
    public Vet getVetById(@PathVariable @NonNull Long id) {
        return vetService.getVetById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteVet(@PathVariable @NonNull Long id) {
        vetService.deleteVet(id);
    }
}
