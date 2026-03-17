package com.pethealth.pet_health.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vital_signs")
public class VitalSign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long petId;

    private Double temperature; // 体温
    private Double weight;      // 体重

    private LocalDateTime recordTime = LocalDateTime.now();
}