package com.pethealth.pet_health.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Entity
@Table(name = "medical_records")
@JsonIgnoreProperties({"pet", "disease", "hibernateLazyInitializer", "handler"}) // 避免序列化循环，并保留疾病摘要字段
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Record title cannot be blank")
    @Column(nullable = false)
    private String title; // 病例标题，如“年度疫苗”

    private String description; // 描述，如症状与处理内容

    private LocalDate visitDate; // 就诊日期，若不填可自动设置

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    @JsonIgnoreProperties("medicalRecords")
    private Pet pet; // 所属宠物

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "disease_id")
    @JsonIgnoreProperties("medicalRecords")
    private Disease disease; // 关联疾病，可为空

    public MedicalRecord() {}

    public MedicalRecord(String title, String description, LocalDate visitDate, Pet pet, Disease disease) {
        this.title = title;
        this.description = description;
        this.visitDate = visitDate;
        this.pet = pet;
        this.disease = disease;
    }

    // ======= getter & setter =======

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public Disease getDisease() { return disease; }
    public void setDisease(Disease disease) { this.disease = disease; }
    @Transient
    public Long getDiseaseId() {
        return disease != null ? disease.getId() : null;
    }

    @Transient
    public String getDiseaseName() {
        return disease != null ? disease.getName() : null;
    }

    @Transient
    public String getDiseaseCategory() {
        return disease != null ? disease.getCategory() : null;
    }

}
