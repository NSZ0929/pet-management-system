package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.Owner;
import com.pethealth.pet_health.entity.Pet;
import com.pethealth.pet_health.service.OwnerService;
import com.pethealth.pet_health.service.PetService;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PetController - 处理与宠物相关的 REST API 请求
 */
@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;
    private final OwnerService ownerService;

    public PetController(PetService petService, OwnerService ownerService) {
        this.petService = petService;
        this.ownerService = ownerService;
    }

    /**
     * 获取所有宠物
     */
    @GetMapping
    public List<Pet> getAllPets() {
        return petService.getAllPets();
    }

    /**
     * 添加新宠物
     */
    @PostMapping
    public Pet addPet(@RequestBody @NonNull Pet pet) {

        Long ownerId = pet.getOwner() != null ? pet.getOwner().getId() : null;
        if (ownerId == null) {
            throw new RuntimeException("Owner ID cannot be null");
        }

        // 获取并验证 Owner 存在
        Owner owner = ownerService.getOwnerById(ownerId);
        pet.setOwner(owner);

        return petService.addPet(pet, ownerId);
    }

    /**
     * 根据 ID 获取宠物
     */
    @GetMapping("/{id}")
    public Pet getPet(@PathVariable @NonNull Long id) {
        return petService.getPetById(id);
    }

    /**
     * 更新宠物信息
     */
    @PutMapping("/{id}")
    public Pet updatePet(@PathVariable @NonNull Long id,
                         @RequestBody @NonNull Pet updatedPet) {
        return petService.updatePet(id, updatedPet);
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/{id}")
    public void deletePet(@PathVariable @NonNull Long id) {
        petService.deletePet(id);
    }

    /**
     * 根据主人 ID 获取宠物列表
     */
    @GetMapping("/owner/{ownerId}")
    public List<Pet> getPetsByOwner(@PathVariable @NonNull Long ownerId) {
        return petService.getPetsByOwner(ownerId);
    }

    /**
     * 根据宠物名字模糊搜索
     */
    @GetMapping("/search")
    public List<Pet> searchPetsByName(@RequestParam @NonNull String name) {
        return petService.searchPetsByName(name);
    }
}
