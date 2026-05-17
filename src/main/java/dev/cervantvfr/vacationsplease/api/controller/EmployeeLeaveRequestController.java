package dev.cervantvfr.vacationsplease.api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.cervantvfr.vacationsplease.api.dto.LeaveRequestResponse;
import dev.cervantvfr.vacationsplease.api.dto.PagedResponse;
import dev.cervantvfr.vacationsplease.application.service.LeaveRequestService;

@RestController
@RequestMapping("api/employees/{employeeId}/leave-requests")
public class EmployeeLeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public EmployeeLeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping
    public PagedResponse<LeaveRequestResponse> listForEmployee(
        @PathVariable Long employeeId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return leaveRequestService.listByEmployee(employeeId, pageable);
    }
}