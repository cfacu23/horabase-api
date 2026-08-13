package com.horabase.api.dashboard;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AdminDashboardResponse(
        Long businessId,
        OffsetDateTime generatedAt,
        LocalDate businessDate,
        long activeEmployees,
        long workingNow,
        long upcomingShifts,
        long lateArrivalsToday,
        long absencesToday,
        long pendingRequests,
        long pendingOvertime
) {
}
