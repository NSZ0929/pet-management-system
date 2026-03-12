package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.Owner;
import com.pethealth.pet_health.entity.Pet;
import com.pethealth.pet_health.repository.PetRepository;
import com.pethealth.pet_health.repository.OwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PetService {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public PetService(PetRepository petRepository, OwnerRepository ownerRepository) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
    }

    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    public Pet addPet(Pet pet, Long ownerId) {
        if (ownerId == null) throw new RuntimeException("Owner ID cannot be null");

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + ownerId));

        pet.setOwner(owner);
        return petRepository.save(pet);
    }

    public Pet getPetById(Long id) {
        if (id == null) throw new RuntimeException("Pet ID cannot be null");

        return petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
    }

    public Pet updatePet(Long id, Pet updatedPet) {
        Pet pet = getPetById(id);

        pet.setName(updatedPet.getName());
        pet.setBreed(updatedPet.getBreed());
        pet.setAge(updatedPet.getAge());

        return petRepository.save(pet);
    }

    public void deletePet(Long id) {
        if (id == null) throw new RuntimeException("Pet ID cannot be null");
        petRepository.deleteById(id);
    }

    public List<Pet> getPetsByOwner(Long ownerId) {
        if (ownerId == null) throw new RuntimeException("Owner ID cannot be null");
        return petRepository.findByOwnerId(ownerId);
    }

    public List<Pet> searchPetsByName(String name) {
        if (name == null || name.isBlank()) throw new RuntimeException("Search name cannot be null or empty");
        return petRepository.findByNameContainingIgnoreCase(name);
    }
}