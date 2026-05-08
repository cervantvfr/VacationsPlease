package dev.cervantvfr.vacationsplease.api.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LeaveRequestResponse(
    Long id,
    Long employeeId,
    String leaveTypeCode,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    String reason,
    OffsetDateTime createdAt
) {}