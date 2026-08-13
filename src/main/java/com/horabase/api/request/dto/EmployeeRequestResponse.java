package com.horabase.api.request.dto;
import com.horabase.api.request.*;import java.time.*;
public record EmployeeRequestResponse(Long id,Long businessId,Long employeeId,String employeeName,EmployeeRequestType type,String title,String description,Long shiftId,LocalDate relatedDate,EmployeeRequestStatus status,String adminResponse,OffsetDateTime resolvedAt,OffsetDateTime createdAt,OffsetDateTime updatedAt){}
