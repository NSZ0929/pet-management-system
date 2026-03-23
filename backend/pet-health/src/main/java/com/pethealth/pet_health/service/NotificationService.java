package com.pethealth.pet_health.service;

import com.pethealth.pet_health.entity.*;
import com.pethealth.pet_health.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository  notificationRepository;
    private final AppointmentRepository   appointmentRepository;
    private final PetRepository           petRepository;
    private final DailyLogRepository      dailyLogRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private JavaMailSender mailSender;

    public NotificationService(
            NotificationRepository  notificationRepository,
            AppointmentRepository   appointmentRepository,
            PetRepository           petRepository,
            DailyLogRepository      dailyLogRepository,
            MedicalRecordRepository medicalRecordRepository) {
        this.notificationRepository  = notificationRepository;
        this.appointmentRepository   = appointmentRepository;
        this.petRepository           = petRepository;
        this.dailyLogRepository      = dailyLogRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    // ─────────────────────────────────────────
    // 1. APPOINTMENT
    // ─────────────────────────────────────────
    @Scheduled(fixedRate = 3_600_000)
    public void generateAppointmentNotifications() {
        logger.info("Scanning for upcoming appointment reminders...");
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime next24h = now.plusHours(24);

        for (Appointment appt : appointmentRepository.findAll()) {
            LocalDateTime apptTime = appt.getAppointmentTime();
            if (apptTime == null || apptTime.isBefore(now) || apptTime.isAfter(next24h)) continue;
            if (notificationRepository.existsByAppointmentId(appt.getId())) continue;

            String timeStr = apptTime.format(DateTimeFormatter.ofPattern("MMM dd 'at' HH:mm"));
            String message = appt.getPet().getName()
                    + " has an appointment with Dr. " + appt.getVet().getName()
                    + " on " + timeStr
                    + (appt.getDescription() != null ? " — " + appt.getDescription() : "");

            save(appt.getPet().getId(), "🐾 Appointment Reminder", message, "APPOINTMENT", appt.getId());
        }
    }

    // ─────────────────────────────────────────
    // 2. VACCINE
    // ─────────────────────────────────────────
    @Scheduled(cron = "0 0 9 * * *")
    public void generateVaccineNotifications() {
        logger.info("Scanning for vaccine renewal reminders...");
        LocalDate today = LocalDate.now();
        LocalDate from  = today.minusDays(370);
        LocalDate to    = today.minusDays(355);

        for (Pet pet : petRepository.findAll()) {
            for (MedicalRecord record : medicalRecordRepository.findByPet(pet)) {
                if (record.getVisitDate() == null) continue;

                String titleLower = record.getTitle().toLowerCase();
                boolean isVaccine = titleLower.contains("vaccine")
                        || titleLower.contains("rabies")
                        || titleLower.contains("booster");

                if (!isVaccine) continue;

                LocalDate visitDate = record.getVisitDate();
                if (visitDate.isAfter(from) && visitDate.isBefore(to)) {
                    String message = pet.getName()
                            + " may need a vaccine renewal soon.";
                    save(pet.getId(), "💉 Vaccine Reminder", message, "VACCINE", null);
                    break;
                }
            }
        }
    }

    // ─────────────────────────────────────────
    // 3. MEDICATION
    // ─────────────────────────────────────────
    @Scheduled(cron = "0 5 9 * * *")
    public void generateMedicationNotifications() {
        logger.info("Scanning for medication reminders...");
        LocalDate today  = LocalDate.now();
        LocalDate cutoff = today.minusDays(30);

        for (Pet pet : petRepository.findAll()) {
            for (MedicalRecord record : medicalRecordRepository.findByPet(pet)) {
                if (record.getVisitDate() == null || record.getVisitDate().isBefore(cutoff)) continue;

                String titleLower = record.getTitle().toLowerCase();
                if (!titleLower.contains("medication")) continue;

                String message = pet.getName() + " may need medication today.";
                save(pet.getId(), "💊 Medication Reminder", message, "MEDICATION", null);
                break;
            }
        }
    }

    // ─────────────────────────────────────────
    // 4. FEEDING
    // ─────────────────────────────────────────
    @Scheduled(cron = "0 0 8 * * *")
    public void generateFeedingNotifications() {
        logger.info("Scanning for feeding reminders...");
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        for (Pet pet : petRepository.findAll()) {
            List<DailyLog> logs = dailyLogRepository
                    .findByPetIdAndTypeAndCreateTimeAfter(pet.getId(), "FEEDING", todayStart);

            if (logs.isEmpty()) {
                String message = pet.getName() + " hasn't been fed yet today.";
                save(pet.getId(), "🍖 Feeding Reminder", message, "FEEDING", null);
            }
        }
    }

    // ─────────────────────────────────────────
    // 核心：统一保存 + 邮件发送
    // ─────────────────────────────────────────
    private void save(Long petId, String title, String message, String type, Long appointmentId) {
        Notification notification = new Notification(petId, title, message, type, appointmentId);
        notificationRepository.save(notification);

        try {
            Pet pet = petRepository.findById(petId).orElse(null);
            if (pet != null && pet.getOwner() != null) {
                String email = pet.getOwner().getContact();

                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(email);
                mail.setSubject(title);
                mail.setText(message);
                mailSender.send(mail);
            }
        } catch (Exception e) {
            logger.error("Email send failed", e);
        }
    }

    // CRUD
    public List<Notification> getByPet(Long petId) {
        return notificationRepository.findByPetIdOrderByCreatedAtDesc(petId);
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(Long petId) {
        List<Notification> list = notificationRepository.findByPetIdOrderByCreatedAtDesc(petId);
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }

    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }

    public long countUnread(Long petId) {
        return notificationRepository.countByPetIdAndIsRead(petId, false);
    }
    // ─────────────────────────────────────────
    // 手动触发（给前端按钮用）
    // ─────────────────────────────────────────
    public void triggerAll() {
        generateAppointmentNotifications();
        generateVaccineNotifications();
        generateMedicationNotifications();
        generateFeedingNotifications();
    }
}