package com.pethealth.pet_health.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Pet - 宠物实体
 */
@Entity
@Table(name = "pets")

public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 数据库主键，自增

    @Column(nullable = false)
    private String name; // 宠物名称

    private String breed; // 品种，可空

    private Integer age; // 年龄，可空

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties("pets") // 防止序列化 Owner 时递归
    private Owner owner;

    public Pet() {}

    public Pet(String name, String breed, Integer age) {
        this.name = name;
        this.breed = breed;
        this.age = age;
    }

    public Pet(String name, String breed, Integer age, Owner owner) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.owner = owner;
    }

    // ======= getter & setter =======
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) { this.owner = owner; }

    private String avatar; // 头像图片路径

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
