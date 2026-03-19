package com.pethealth.pet_health.repository;

import com.pethealth.pet_health.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 查询某只宠物的所有通知，按时间倒序
    List<Notification> findByPetIdOrderByCreatedAtDesc(Long petId);

    // 查询未读通知数量
    long countByPetIdAndIsRead(Long petId, Boolean isRead);

    // 检查某个预约是否已经生成过通知（防止重复）
    boolean existsByAppointmentId(Long appointmentId);
}