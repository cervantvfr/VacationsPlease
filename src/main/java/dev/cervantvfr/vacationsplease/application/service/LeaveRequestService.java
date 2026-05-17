package dev.cervantvfr.vacationsplease.application.service;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.cervantvfr.vacationsplease.api.dto.LeaveRequestResponse;
import dev.cervantvfr.vacationsplease.api.dto.PagedResponse;
import dev.cervantvfr.vacationsplease.api.mapper.LeaveRequestApiMapper;
import dev.cervantvfr.vacationsplease.application.exception.BusinessRuleViolationException;
import dev.cervantvfr.vacationsplease.application.exception.ResourceNotFoundException;
import dev.cervantvfr.vacationsplease.domain.enums.LeaveRequestStatus;
import dev.cervantvfr.vacationsplease.domain.model.Employee;
import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;
import dev.cervantvfr.vacationsplease.domain.model.LeaveType;
import dev.cervantvfr.vacationsplease.domain.repository.EmployeeRepository;
import dev.cervantvfr.vacationsplease.domain.repository.LeaveRequestRepository;
import dev.cervantvfr.vacationsplease.domain.repository.LeaveTypeRepository;

@Service
public class LeaveRequestService {
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestService(
            EmployeeRepository employeeRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeaveRequestRepository leaveRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Transactional
    public LeaveRequestResponse submitRequest(
        Long employeeId,
        String leaveTypeCode,
        LocalDate startDate,
        LocalDate endDate,
        String reason
    ) {
        if (startDate == null || endDate == null) {
            throw new BusinessRuleViolationException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleViolationException("End date cannot be before start date");
        }

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found " + employeeId));

        LeaveType leaveType = leaveTypeRepository.findByCode(leaveTypeCode)
            .orElseThrow(() -> new ResourceNotFoundException("Leave type not found " + leaveTypeCode));

        boolean overlap = leaveRequestRepository.existsOverlappingPendingOrApproved(employee.getId(), startDate, endDate); 
        if (overlap) {
            throw new BusinessRuleViolationException("Overlapping leave requests already exists");
        }

        LeaveRequest request = new LeaveRequest(
            employee,
            leaveType,
            startDate,
            endDate,
            LeaveRequestStatus.PENDING,
            reason
        );
        LeaveRequest saved = leaveRequestRepository.save(request);
        return LeaveRequestApiMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponse getById(Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found " + id));
        return LeaveRequestApiMapper.toResponse(request);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LeaveRequestResponse> listAll(Pageable pageable) {
        return LeaveRequestApiMapper.toPagedResponse(
                leaveRequestRepository.findAllByOrderByCreatedAtDesc(pageable));
    }

    @Transactional(readOnly = true)
    public PagedResponse<LeaveRequestResponse> listByEmployee(Long employeeId, Pageable pageable) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found " + employeeId);
        }
        return LeaveRequestApiMapper.toPagedResponse(
                leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable));
    }
    
    // Rollback proof test
    // @Transactional
    // public void submitRequestThenFail(
    //         Long employeeId,
    //         String leaveTypeCode,
    //         LocalDate startDate,
    //         LocalDate endDate,
    //         String reason
    // ) {

    //     LeaveRequest saved = submitRequest(employeeId, leaveTypeCode, startDate, endDate, reason);
        
    //     throw new RuntimeException("Forced failure. request id: " + saved.getId());
    // }
}