package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.Notification;
import com.pethealth.pet_health.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 获取某只宠物的所有通知
    // GET /notifications?petId=1
    @GetMapping
    public List<Notification> getByPet(@RequestParam Long petId) {
        return notificationService.getByPet(petId);
    }

    // 获取未读数量
    // GET /notifications/unread-count?petId=1
    @GetMapping("/unread-count")
    public Map<String, Long> countUnread(@RequestParam Long petId) {
        return Map.of("count", notificationService.countUnread(petId));
    }

    // 标记单条为已读
    // PUT /notifications/{id}/read
    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    // 标记全部为已读
    // PUT /notifications/read-all?petId=1
    @PutMapping("/read-all")
    public void markAllAsRead(@RequestParam Long petId) {
        notificationService.markAllAsRead(petId);
    }

    // 删除通知
    // DELETE /notifications/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        notificationService.delete(id);
    }
}