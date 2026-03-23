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

    // GET /notifications?petId=1
    @GetMapping
    public List<Notification> getByPet(@RequestParam Long petId) {
        return notificationService.getByPet(petId);
    }

    // GET /notifications/unread-count?petId=1
    @GetMapping("/unread-count")
    public Map<String, Long> countUnread(@RequestParam Long petId) {
        return Map.of("count", notificationService.countUnread(petId));
    }

    // PUT /notifications/{id}/read
    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    // PUT /notifications/read-all?petId=1
    @PutMapping("/read-all")
    public void markAllAsRead(@RequestParam Long petId) {
        notificationService.markAllAsRead(petId);
    }

    // DELETE /notifications/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        notificationService.delete(id);
    }

    // POST /notifications/trigger-scan
    // Manually triggers ALL notification scanners at once (appointments + vaccine + medication + feeding)
    @PostMapping("/trigger-scan")
    public Map<String, String> triggerScan() {
        notificationService.triggerAll();
        return Map.of("status", "All notification scanners triggered successfully");
    }
}