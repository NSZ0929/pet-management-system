package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.Appointment;
import com.pethealth.pet_health.entity.Pet;
import com.pethealth.pet_health.entity.Vet;
import com.pethealth.pet_health.repository.AppointmentRepository;
import com.pethealth.pet_health.repository.PetRepository;
import com.pethealth.pet_health.repository.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SuppressWarnings("null")
@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final VetRepository vetRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PetRepository petRepository,
            VetRepository vetRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
    }

    /**
     * 获取所有预约
     */
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    /**
     * 添加新预约
     */
    public Appointment addAppointment(Appointment appointment, Long petId, Long vetId) {
        if (appointment == null) throw new RuntimeException("Appointment cannot be null");
        if (petId == null) throw new RuntimeException("Pet ID cannot be null");
        if (vetId == null) throw new RuntimeException("Vet ID cannot be null");

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + petId));

        Vet vet = vetRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Vet not found with id: " + vetId));

        appointment.setPet(pet);
        appointment.setVet(vet);

        return appointmentRepository.save(appointment);
    }

    /**
     * 根据 ID 获取预约
     */
    public Appointment getAppointmentById(Long id) {
        if (id == null) throw new RuntimeException("Appointment ID cannot be null");

        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
    }

    /**
     * 更新预约
     */
    public Appointment updateAppointment(Long id, Appointment updated, Long vetId) {
        if (updated == null) throw new RuntimeException("Updated appointment cannot be null");

        Appointment appointment = getAppointmentById(id);

        // 允许用户只更新部分字段
        if (updated.getAppointmentTime() != null) {
            appointment.setAppointmentTime(updated.getAppointmentTime());
        }
        if (updated.getDescription() != null) {
            appointment.setDescription(updated.getDescription());
        }
        if (vetId != null) {
            Vet vet = vetRepository.findById(vetId)
                    .orElseThrow(() -> new RuntimeException("Vet not found with id: " + vetId));
            appointment.setVet(vet);
        }

        return appointmentRepository.save(appointment);
    }

    /**
     * 删除预约
     */
    public void deleteAppointment(Long id) {
        if (id == null) throw new RuntimeException("Appointment ID cannot be null");

        appointmentRepository.deleteById(id);
    }

    /**
     * 获取某宠物的所有预约
     */
    public List<Appointment> getAppointmentsByPet(Long petId) {
        if (petId == null) throw new RuntimeException("Pet ID cannot be null");
        return appointmentRepository.findByPetId(petId);
    }

    /**
     * 获取某医生的所有预约
     */
    public List<Appointment> getAppointmentsByVet(Long vetId) {
        if (vetId == null) throw new RuntimeException("Vet ID cannot be null");
        return appointmentRepository.findByVetId(vetId);
    }
}
