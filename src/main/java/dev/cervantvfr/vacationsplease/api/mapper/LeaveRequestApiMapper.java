package dev.cervantvfr.vacationsplease.api.mapper;

import dev.cervantvfr.vacationsplease.api.dto.LeaveRequestResponse;
import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;

public final class LeaveRequestApiMapper {

    private LeaveRequestApiMapper() {}

    public static LeaveRequestResponse toResponse(LeaveRequest request) {
        return new LeaveRequestResponse(
            request.getId(),
            request.getEmployee().getId(),
            request.getLeaveType().getCode(),
            request.getStartDate(),
            request.getEndDate(),
            request.getStatus().name(),
            request.getReason(),
            request.getCreatedAt()
        );
    }
}