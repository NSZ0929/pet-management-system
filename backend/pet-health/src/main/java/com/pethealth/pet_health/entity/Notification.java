package com.pethealth.pet_health.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联的宠物ID（用于前端按宠物过滤）
    private Long petId;

    // 通知标题
    @Column(nullable = false)
    private String title;

    // 通知内容
    private String message;

    // 通知类型：APPOINTMENT / VACCINE / MEDICATION
    private String type;

    // 是否已读
    private Boolean isRead = false;

    // 创建时间
    private LocalDateTime createdAt = LocalDateTime.now();

    // 关联的预约ID（可选，方便跳转）
    private Long appointmentId;

    public Notification(Long petId, String title, String message, String type, Long appointmentId) {
        this.petId = petId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.appointmentId = appointmentId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}