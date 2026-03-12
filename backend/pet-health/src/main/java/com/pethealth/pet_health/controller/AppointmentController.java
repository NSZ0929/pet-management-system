package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.Appointment;
import com.pethealth.pet_health.service.AppointmentService;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @PostMapping
    public Appointment addAppointment(
            @RequestBody @NonNull Appointment appointment,
            @RequestParam @NonNull Long petId,
            @RequestParam @NonNull Long vetId
    ) {
        // service 内部已保证保存的对象非空，但在 Controller 再次断言，完全消除 IDE 警告
        return Objects.requireNonNull(
                appointmentService.addAppointment(appointment, petId, vetId),
                "Failed to create appointment"
        );
    }

    @GetMapping("/{id}")
    public Appointment getAppointmentById(@PathVariable @NonNull Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @PutMapping("/{id}")
    public Appointment updateAppointment(
            @PathVariable @NonNull Long id,
            @RequestBody @NonNull Appointment updated
    ) {
        return Objects.requireNonNull(
                appointmentService.updateAppointment(id, updated),
                "Failed to update appointment"
        );
    }

    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable @NonNull Long id) {
        appointmentService.deleteAppointment(id);
    }
}
