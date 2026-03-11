package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.Disease;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {

    List<Disease> findByNameContainingIgnoreCase(String name); }
