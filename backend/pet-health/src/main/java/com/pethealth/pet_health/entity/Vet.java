package com.pethealth.pet_health.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Vet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 兽医名字
    @Column(nullable = false)
    private String name;

    // 专业领域，例如：皮肤科、外科、牙科等
    private String specialty;
}
