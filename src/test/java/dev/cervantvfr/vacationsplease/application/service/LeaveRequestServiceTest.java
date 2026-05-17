package dev.cervantvfr.vacationsplease.application.service;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.cervantvfr.vacationsplease.api.dto.LeaveRequestResponse;
import dev.cervantvfr.vacationsplease.application.exception.BusinessRuleViolationException;
import dev.cervantvfr.vacationsplease.application.exception.ResourceNotFoundException;
import dev.cervantvfr.vacationsplease.domain.enums.LeaveRequestStatus;
import dev.cervantvfr.vacationsplease.domain.model.Employee;
import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;
import dev.cervantvfr.vacationsplease.domain.model.LeaveType;
import dev.cervantvfr.vacationsplease.domain.repository.EmployeeRepository;
import dev.cervantvfr.vacationsplease.domain.repository.LeaveRequestRepository;
import dev.cervantvfr.vacationsplease.domain.repository.LeaveTypeRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})

class LeaveRequestServiceTest {

    @Autowired
    LeaveRequestService leaveRequestService;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    LeaveTypeRepository leaveTypeRepository;

    @Autowired
    LeaveRequestRepository leaveRequestRepository;

    private Employee employee;
    private LeaveType leaveType;

    @BeforeEach
    void setUp() {
        leaveRequestRepository.deleteAll();
        leaveTypeRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = employeeRepository.save(new Employee("john@test.com", "John", "Test"));
        leaveType = leaveTypeRepository.save(new LeaveType("PTO", "Paid Time Off", true));
    }

    @Test
    void submitRequest_savesPendingRequest_whenValid() {
        LeaveRequestResponse saved = leaveRequestService.submitRequest(
            employee.getId(),
            leaveType.getCode(),
            LocalDate.of(2026, 7, 10),
            LocalDate.of(2026, 7, 12),
            "Family trip"
        );

        assertNotNull(saved.id());
        assertEquals("PENDING", saved.status());
        assertEquals(employee.getId(), saved.employeeId());
        assertEquals(1, leaveRequestRepository.count());
    }

    @Test
    void submitRequest_throws_whenEndDateBeforeStartDate() {
        assertThrows(BusinessRuleViolationException.class, () ->
            leaveRequestService.submitRequest(
                employee.getId(),
                leaveType.getCode(),
                LocalDate.of(2026, 7, 12),
                LocalDate.of(2026, 7, 10),
                "Invalid range"
            )
        );
    }

    @Test
    void submitRequest_throws_whenOverlappingPendingRequestExists() {
        leaveRequestRepository.save(new LeaveRequest(
            employee,
            leaveType,
            LocalDate.of(2026, 7, 10),
            LocalDate.of(2026, 7, 12),
            LeaveRequestStatus.PENDING,
            "Existing"
        ));

        assertThrows(BusinessRuleViolationException.class, () ->
            leaveRequestService.submitRequest(
                employee.getId(),
                leaveType.getCode(),
                LocalDate.of(2026, 7, 11),
                LocalDate.of(2026, 7, 13),
                "Overlapping"
            )
        );
    }

    @Test
    void submitRequest_throws_whenEmployeeNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
            leaveRequestService.submitRequest(
                999999L,
                leaveType.getCode(),
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                "Unknown employee"
            )
        );
    }

    @Test
    void submitRequest_throws_whenLeaveTypeNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
            leaveRequestService.submitRequest(
                employee.getId(),
                "UNKNOWN",
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                "Unknown leave type"
            )
        );
    }

    // Rollback proof test
    // @Test
    // void submitRequestThenFail_rollsBackSavedRequest() {
    //     long before = leaveRequestRepository.count();

    //     assertThrows(RuntimeException.class, () ->
    //         leaveRequestService.submitRequestThenFail(
    //             employee.getId(),
    //             leaveType.getCode(),
    //             LocalDate.of(2026, 8, 1),
    //             LocalDate.of(2026, 8, 3),
    //             "Rollback proof"
    //         )
    //     );

    //     long after = leaveRequestRepository.count();
    //     assertEquals(before, after, "Count should be the same");        
    // }
}