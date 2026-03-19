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

    /**
     * 获取所有预约，或按宠物ID筛选
     * GET /appointments
     * GET /appointments?petId=1
     */
    @GetMapping
    public List<Appointment> getAppointments(
            @RequestParam(required = false) Long petId
    ) {
        if (petId != null) {
            return appointmentService.getAppointmentsByPet(petId);
        }
        return appointmentService.getAllAppointments();
    }

    /**
     * 新增预约
     * POST /appointments?petId=1&vetId=2
     */
    @PostMapping
    public Appointment addAppointment(
            @RequestBody @NonNull Appointment appointment,
            @RequestParam @NonNull Long petId,
            @RequestParam @NonNull Long vetId
    ) {
        return Objects.requireNonNull(
                appointmentService.addAppointment(appointment, petId, vetId),
                "Failed to create appointment"
        );
    }

    /**
     * 根据ID获取预约
     * GET /appointments/{id}
     */
    @GetMapping("/{id}")
    public Appointment getAppointmentById(@PathVariable @NonNull Long id) {
        return appointmentService.getAppointmentById(id);
    }

    /**
     * 更新预约
     * PUT /appointments/{id}
     */
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

    /**
     * 删除预约
     * DELETE /appointments/{id}
     */
    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable @NonNull Long id) {
        appointmentService.deleteAppointment(id);
    }
}