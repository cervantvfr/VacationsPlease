package dev.cervantvfr.vacationsplease.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitLeaveRequestRequest(
    @NotNull Long employeeId,
    @NotNull String leaveTypeCode,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @Size(max = 1000) String reason
) {}