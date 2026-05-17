package dev.cervantvfr.vacationsplease.api.controller;

import java.net.URI;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import dev.cervantvfr.vacationsplease.api.dto.LeaveRequestResponse;
import dev.cervantvfr.vacationsplease.api.dto.PagedResponse;
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

    @GetMapping
    public PagedResponse<LeaveRequestResponse> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return leaveRequestService.listAll(pageable);
    }
    
    @GetMapping("/{id}")
    public LeaveRequestResponse getById(@PathVariable Long id) {
        return leaveRequestService.getById(id);
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> submitLeaveRequest(
            @Valid @RequestBody SubmitLeaveRequestRequest body) {
        LeaveRequestResponse response = leaveRequestService.submitRequest(
            body.employeeId(),
            body.leaveTypeCode(),
            body.startDate(),
            body.endDate(),
            body.reason()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}