package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.Vet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VetRepository extends JpaRepository<Vet, Long> {
}
