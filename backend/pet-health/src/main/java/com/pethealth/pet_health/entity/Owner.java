package com.pethealth.pet_health.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

/**
 * Owner - 主人实体
 */
@Entity
@Table(name = "owners")
@JsonIgnoreProperties("pets") // 防止序列化时递归
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 数据库主键，自增

    @NotBlank(message = "Owner name cannot be blank")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Contact information cannot be blank")
    @Column(nullable = false)
    private String contact;

    private String address;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pet> pets = new HashSet<>();

    public Owner() {}

    public Owner(String name, String contact, String address) {
        this.name = name;
        this.contact = contact;
        this.address = address;
    }

    // ======= getter & setter =======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Set<Pet> getPets() { return pets; }
    public void setPets(Set<Pet> pets) { this.pets = pets; }

    // ======= 双向关系维护 =======
    public void addPet(Pet pet) {
        if (pet != null) {
            pets.add(pet);
            pet.setOwner(this);
        }
    }

    public void removePet(Pet pet) {
        if (pet != null) {
            pets.remove(pet);
            pet.setOwner(null);
        }
    }
}
