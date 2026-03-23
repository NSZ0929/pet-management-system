package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a pet, newest first
    List<Notification> findByPetIdOrderByCreatedAtDesc(Long petId);

    // Count unread notifications for a pet
    long countByPetIdAndIsRead(Long petId, Boolean isRead);

    // Check if a notification already exists for a given appointment (dedup)
    boolean existsByAppointmentId(Long appointmentId);

    // Check if a notification of a given type already exists for a pet within a time window (dedup for daily/vaccine)
    boolean existsByPetIdAndTypeAndCreatedAtAfter(Long petId, String type, LocalDateTime after);
}