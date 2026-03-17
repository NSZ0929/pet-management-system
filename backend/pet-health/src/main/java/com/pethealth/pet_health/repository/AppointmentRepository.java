package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPetId(Long petId);

    List<Appointment> findByVetId(Long vetId);
}
