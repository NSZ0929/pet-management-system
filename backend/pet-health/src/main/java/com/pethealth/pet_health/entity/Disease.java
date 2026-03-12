package com.pethealth.pet_health.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "diseases")
public class Disease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;      // 疾病名，如 "皮肤炎"

    private String category;  // 类型，如 "皮肤科 / 内科 / 外科"

    private String description; // 描述（可选）
}
