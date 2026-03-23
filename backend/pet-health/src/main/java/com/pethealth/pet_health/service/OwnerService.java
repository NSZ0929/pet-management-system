package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.Owner;
import com.pethealth.pet_health.repository.OwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OwnerService - 主人业务逻辑层
 */
@Service
@Transactional
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * 获取所有主人
     */
    public List<Owner> getAllOwners() {
        return ownerRepository.findAll();
    }

    /**
     * 添加新主人
     */
    public Owner addOwner(Owner owner) {
        if (owner == null) throw new RuntimeException("Owner cannot be null");
        return ownerRepository.save(owner);
    }

    /**
     * 根据 ID 获取主人
     */
    public Owner getOwnerById(Long id) {
        if (id == null) throw new RuntimeException("Owner ID cannot be null");
        return ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + id));
    }

    /**
     * 更新主人信息
     */
    public Owner updateOwner(Long id, Owner updatedOwner) {
        Owner owner = getOwnerById(id);
        owner.setName(updatedOwner.getName());
        owner.setContact(updatedOwner.getContact());
        owner.setAddress(updatedOwner.getAddress());
        owner.setEnvironment(updatedOwner.getEnvironment());
        owner.setHomeTemperature(updatedOwner.getHomeTemperature());
        owner.setOtherPets(updatedOwner.getOtherPets());
        owner.setMembers(updatedOwner.getMembers());
        return ownerRepository.save(owner);
    }

    /**
     * 删除主人
     */
    public void deleteOwner(Long id) {
        if (id == null) throw new RuntimeException("Owner ID cannot be null");
        ownerRepository.deleteById(id);
    }

    /**
     * 根据姓名模糊搜索主人
     */
    public List<Owner> searchOwnersByName(String name) {
        if (name == null || name.isBlank()) throw new RuntimeException("Search name cannot be null or empty");
        return ownerRepository.findByNameContainingIgnoreCase(name);
    }
}
