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
     * 每小时执行一次：扫描未来24小时内的预约，生成通知
     */
    @Scheduled(fixedRate = 3600000)
    public void generateAppointmentNotifications() {
        logger.info("开始扫描预约提醒...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24h = now.plusHours(24);

        // 查询所有预约
        List<Appointment> allAppointments = appointmentRepository.findAll();

        for (Appointment appt : allAppointments) {
            LocalDateTime apptTime = appt.getAppointmentTime();

            // 只处理未来24小时内的预约
            if (apptTime == null || apptTime.isBefore(now) || apptTime.isAfter(next24h)) {
                continue;
            }

            // 已经生成过通知的跳过（防止重复）
            if (notificationRepository.existsByAppointmentId(appt.getId())) {
                continue;
            }

            // 格式化时间
            String timeStr = apptTime.format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm"));

            // 生成通知
            Notification notification = new Notification(
                appt.getPet().getId(),
                "🐾 预约提醒",
                appt.getPet().getName() + " 将于 " + timeStr +
                    " 与 " + appt.getVet().getName() + " 医生进行预约" +
                    (appt.getDescription() != null ? "：" + appt.getDescription() : ""),
                "APPOINTMENT",
                appt.getId()
            );

            notificationRepository.save(notification);
            logger.info("已生成通知：{}", notification.getMessage());
        }
    }

    // 获取某只宠物的所有通知
    public List<Notification> getByPet(Long petId) {
        return notificationRepository.findByPetIdOrderByCreatedAtDesc(petId);
    }

    // 标记某条通知为已读
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    // 标记某只宠物的所有通知为已读
    public void markAllAsRead(Long petId) {
        List<Notification> list = notificationRepository.findByPetIdOrderByCreatedAtDesc(petId);
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }

    // 删除通知
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }

    // 获取未读数量
    public long countUnread(Long petId) {
        return notificationRepository.countByPetIdAndIsRead(petId, false);
    }
}