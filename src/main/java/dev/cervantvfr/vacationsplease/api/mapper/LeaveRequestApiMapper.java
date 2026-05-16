package dev.cervantvfr.vacationsplease.api.mapper;

import java.util.List;
import org.springframework.data.domain.Page;

import dev.cervantvfr.vacationsplease.api.dto.LeaveRequestResponse;
import dev.cervantvfr.vacationsplease.api.dto.PagedResponse;
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

    public static List<LeaveRequestResponse> toResponse(List<LeaveRequest> requests) {
        return requests.stream()
            .map(LeaveRequestApiMapper::toResponse)
            .toList();
    }

    public static PagedResponse<LeaveRequestResponse> toPagedResponse(Page<LeaveRequest> page) {
        return new PagedResponse<>(
            toResponse(page.getContent()),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}