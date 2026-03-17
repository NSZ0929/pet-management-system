package com.pethealth.pet_health.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "daily_logs")
public class DailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long petId;

    // 类型：FEEDING / EXCRETION / SLEEP / ACTIVITY / MOOD
    @Column(nullable = false)
    private String type;

    // 内容（前端传 JSON 字符串）
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createTime = LocalDateTime.now();
}