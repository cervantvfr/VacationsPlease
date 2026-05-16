package dev.cervantvfr.vacationsplease.api.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.cervantvfr.vacationsplease.api.dto.LeaveRequestResponse;
import dev.cervantvfr.vacationsplease.api.dto.SubmitLeaveRequestRequest;
import dev.cervantvfr.vacationsplease.api.mapper.LeaveRequestApiMapper;
import dev.cervantvfr.vacationsplease.application.service.LeaveRequestService;
import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;

@RestController
@RequestMapping("api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> submitLeaveRequest(
        @Valid @RequestBody SubmitLeaveRequestRequest body
    ) {
        LeaveRequest created = leaveRequestService.submitRequest(
            body.employeeId(),
            body.leaveTypeCode(),
            body.startDate(),
            body.endDate(),
            body.reason()
        );

        LeaveRequestResponse response = LeaveRequestApiMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}