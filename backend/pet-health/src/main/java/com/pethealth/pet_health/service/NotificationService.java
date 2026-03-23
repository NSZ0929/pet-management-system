package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.Appointment;
import com.pethealth.pet_health.entity.Notification;
import com.pethealth.pet_health.repository.AppointmentRepository;
import com.pethealth.pet_health.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;

    public NotificationService(NotificationRepository notificationRepository,
                                AppointmentRepository appointmentRepository) {
        this.notificationRepository = notificationRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Runs every hour: scans appointments within the next 24 hours and generates notifications.
     */
    @Scheduled(fixedRate = 3600000)
    public void generateAppointmentNotifications() {
        logger.info("Scanning for upcoming appointment reminders...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24h = now.plusHours(24);

        List<Appointment> allAppointments = appointmentRepository.findAll();

        for (Appointment appt : allAppointments) {
            LocalDateTime apptTime = appt.getAppointmentTime();

            // Only process appointments within the next 24 hours
            if (apptTime == null || apptTime.isBefore(now) || apptTime.isAfter(next24h)) {
                continue;
            }

            // Skip if a notification has already been generated for this appointment
            if (notificationRepository.existsByAppointmentId(appt.getId())) {
                continue;
            }

            // Format time as e.g. "Mar 23 at 19:00"
            String timeStr = apptTime.format(DateTimeFormatter.ofPattern("MMM dd 'at' HH:mm"));

            // Build English notification message
            String message = appt.getPet().getName()
                    + " has an appointment with Dr. " + appt.getVet().getName()
                    + " on " + timeStr
                    + (appt.getDescription() != null ? " — " + appt.getDescription() : "");

            Notification notification = new Notification(
                appt.getPet().getId(),
                "🐾 Appointment Reminder",
                message,
                "APPOINTMENT",
                appt.getId()
            );

            notificationRepository.save(notification);
            logger.info("Notification generated: {}", notification.getMessage());
        }
    }

    // Get all notifications for a pet
    public List<Notification> getByPet(Long petId) {
        return notificationRepository.findByPetIdOrderByCreatedAtDesc(petId);
    }

    // Mark a single notification as read
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    // Mark all notifications for a pet as read
    public void markAllAsRead(Long petId) {
        List<Notification> list = notificationRepository.findByPetIdOrderByCreatedAtDesc(petId);
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }

    // Delete a notification
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }

    // Get unread count for a pet
    public long countUnread(Long petId) {
        return notificationRepository.countByPetIdAndIsRead(petId, false);
    }
}