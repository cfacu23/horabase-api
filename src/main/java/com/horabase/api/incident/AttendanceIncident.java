package com.horabase.api.incident;

import com.horabase.api.attendance.Attendance;
import com.horabase.api.business.Business;
import com.horabase.api.employee.Employee;
import com.horabase.api.shift.Shift;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity @Table(name="attendance_incidents")
public class AttendanceIncident {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="business_id",nullable=false) private Business business;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="employee_id",nullable=false) private Employee employee;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="shift_id") private Shift shift;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="attendance_id") private Attendance attendance;
 @Column(name="incident_date",nullable=false) private LocalDate incidentDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private IncidentType type;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private IncidentStatus status;
 @Column(name="detected_minutes") private Long detectedMinutes;
 @Column(length=1000) private String notes;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
 @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @PrePersist void create(){createdAt=updatedAt=OffsetDateTime.now();} @PreUpdate void update(){updatedAt=OffsetDateTime.now();}
 public Long getId(){return id;} public Business getBusiness(){return business;} public void setBusiness(Business v){business=v;}
 public Employee getEmployee(){return employee;} public void setEmployee(Employee v){employee=v;} public Shift getShift(){return shift;} public void setShift(Shift v){shift=v;}
 public Attendance getAttendance(){return attendance;} public void setAttendance(Attendance v){attendance=v;} public LocalDate getIncidentDate(){return incidentDate;} public void setIncidentDate(LocalDate v){incidentDate=v;}
 public IncidentType getType(){return type;} public void setType(IncidentType v){type=v;} public IncidentStatus getStatus(){return status;} public void setStatus(IncidentStatus v){status=v;}
 public Long getDetectedMinutes(){return detectedMinutes;} public void setDetectedMinutes(Long v){detectedMinutes=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
 public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
