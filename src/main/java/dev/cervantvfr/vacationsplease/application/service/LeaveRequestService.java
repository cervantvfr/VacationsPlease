package dev.cervantvfr.vacationsplease.application.service;

import org.springframework.transaction.annotation.Transactional;

import dev.cervantvfr.vacationsplease.domain.repository.EmployeeRepository;
import dev.cervantvfr.vacationsplease.domain.repository.LeaveTypeRepository;
import dev.cervantvfr.vacationsplease.domain.repository.LeaveRequestRepository;
import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;
import dev.cervantvfr.vacationsplease.domain.model.Employee;
import dev.cervantvfr.vacationsplease.domain.model.LeaveType;
import dev.cervantvfr.vacationsplease.domain.enums.LeaveRequestStatus;
import dev.cervantvfr.vacationsplease.application.exception.BusinessRuleViolationException;
import dev.cervantvfr.vacationsplease.application.exception.ResourceNotFoundException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class LeaveRequestService {
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestService(EmployeeRepository employeeRepository, LeaveTypeRepository leaveTypeRepository, LeaveRequestRepository leaveRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Transactional
    public LeaveRequest submitRequest(
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

        return leaveRequestRepository.save(request);
    }
}