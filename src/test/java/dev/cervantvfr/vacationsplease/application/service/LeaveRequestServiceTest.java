package dev.cervantvfr.vacationsplease.application.service;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import dev.cervantvfr.vacationsplease.api.dto.LeaveRequestResponse;
import dev.cervantvfr.vacationsplease.api.dto.PagedResponse;
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

    private LeaveRequest saveRequest(Employee employee, LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository.save(new LeaveRequest(
            employee,
            leaveType,
            startDate,
            endDate,
            LeaveRequestStatus.PENDING,
            "Test"
        ));
    }

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

    // Confirms read transaction + dto mapping
    @Test
    void getById_returnsMappedDto_whenExists() {
        LeaveRequest saved = saveRequest(
            employee, leaveType,
            LocalDate.of(2026, 7, 10),
            LocalDate.of(2026, 7, 12)
        );

        LeaveRequestResponse response = leaveRequestService.getById(saved.getId());

        assertEquals(saved.getId(), response.id());
        assertEquals(employee.getId(), response.employeeId());
        assertEquals("PTO", response.leaveTypeCode());
        assertEquals("PENDING", response.status());
    }
    
    @Test
    void getById_throws_whenNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                leaveRequestService.getById(999999L)
            );
    }

    // Proves Pageable is passed through and PagedResponse metadata matches DB data
    @Test
    void listAll_returnsPagedResults() {
        saveRequest(employee, leaveType, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 2));
        saveRequest(employee, leaveType, LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 4));
        saveRequest(employee, leaveType, LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 6));

        PagedResponse<LeaveRequestResponse> page = leaveRequestService.listAll(
            PageRequest.of(0, 2)
        );

        assertEquals(2, page.content().size());
        assertEquals(3, page.totalElements());
        assertEquals(2, page.totalPages());
        assertTrue(page.first());
        assertFalse(page.last());
    }

    
    // Guards against broken query that returns all requests
    @Test
    void listByEmployee_returnsSingleEmployeeRequests() {
        Employee otherEmployee = employeeRepository.save(new Employee("michael@test.com", "Michael", "Test"));

        saveRequest(employee, leaveType, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 12));
        saveRequest(employee, leaveType, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3));
        saveRequest(otherEmployee, leaveType, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3));

        PagedResponse<LeaveRequestResponse> page = leaveRequestService.listByEmployee(
            employee.getId(),
            PageRequest.of(0, 20)
        );

        assertEquals(2, page.content().size());
        assertEquals(2, page.totalElements());
        assertTrue(page.content().stream().allMatch(r -> r.employeeId().equals(employee.getId())));
    }

    // Documens policy: unknown parent -> 404 error
    @Test
    void listByEmployee_throws_whenEmployeeNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                leaveRequestService.listByEmployee(999999L, PageRequest.of(0, 20))
        );
    }

    // Distinguishes between "employee exists but has no requests" and "employee does not exist"
    @Test
    void listByEmployee_returnsEmptyPage_whenEmployeeHasNoRequests() {
        PagedResponse<LeaveRequestResponse> page = leaveRequestService.listByEmployee(
            employee.getId(),
            PageRequest.of(0, 20)
        );

        assertTrue(page.content().isEmpty());
        assertEquals(0, page.totalElements());
        assertEquals(0, page.totalPages());
        assertTrue(page.first());
        assertTrue(page.last());
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