package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.VitalSign;
import com.pethealth.pet_health.service.VitalSignService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vital-signs")
public class VitalSignController {

    private final VitalSignService service;

    public VitalSignController(VitalSignService service) {
        this.service = service;
    }

    @PostMapping
    public VitalSign add(@RequestBody VitalSign vs) {
        return service.add(vs);
    }

    @GetMapping
    public List<VitalSign> get(@RequestParam Long petId) {
        return service.getByPet(petId);
    }
        @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}