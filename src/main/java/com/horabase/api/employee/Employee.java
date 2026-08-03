package com.horabase.api.employee;

import com.horabase.api.account.Account;
import com.horabase.api.business.Business;
import com.horabase.api.sector.Sector;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_account",
                        columnNames = "account_id"
                )
        }
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "business_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_employee_business")
    )
    private Business business;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_employee_account")
    )
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "sector_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_employee_sector")
    )
    private Sector sector;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(
            name = "hourly_rate",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal hourlyRate;

    @Column(
            name = "overtime_hourly_rate",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal overtimeHourlyRate;

    @Column(
            name = "weekly_hours",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal weeklyHours;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Employee() {
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getOvertimeHourlyRate() {
        return overtimeHourlyRate;
    }

    public void setOvertimeHourlyRate(BigDecimal overtimeHourlyRate) {
        this.overtimeHourlyRate = overtimeHourlyRate;
    }

    public BigDecimal getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(BigDecimal weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}