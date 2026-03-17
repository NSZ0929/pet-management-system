package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.VitalSign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VitalSignRepository extends JpaRepository<VitalSign, Long> {

    List<VitalSign> findByPetId(Long petId);
}