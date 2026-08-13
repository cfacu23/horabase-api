package com.horabase.api.attendance;

import com.horabase.api.business.Business;
import com.horabase.api.employee.Employee;
import com.horabase.api.shift.Shift;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "attendances",
        indexes = {
                @Index(
                        name = "idx_attendance_business_check_in",
                        columnList = "business_id, check_in_at"
                ),
                @Index(
                        name = "idx_attendance_employee_status",
                        columnList = "employee_id, status"
                )
        }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "business_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_attendance_business")
    )
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_attendance_employee")
    )
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "shift_id",
            foreignKey = @ForeignKey(name = "fk_attendance_shift")
    )
    private Shift shift;

    @Column(name = "check_in_at", nullable = false)
    private OffsetDateTime checkInAt;

    @Column(name = "check_out_at")
    private OffsetDateTime checkOutAt;

    @Column(name = "worked_minutes")
    private Long workedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceOrigin origin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Attendance() {
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public OffsetDateTime getCheckInAt() {
        return checkInAt;
    }

    public void setCheckInAt(OffsetDateTime checkInAt) {
        this.checkInAt = checkInAt;
    }

    public OffsetDateTime getCheckOutAt() {
        return checkOutAt;
    }

    public void setCheckOutAt(OffsetDateTime checkOutAt) {
        this.checkOutAt = checkOutAt;
    }

    public Long getWorkedMinutes() {
        return workedMinutes;
    }

    public void setWorkedMinutes(Long workedMinutes) {
        this.workedMinutes = workedMinutes;
    }

    public AttendanceOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(AttendanceOrigin origin) {
        this.origin = origin;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
