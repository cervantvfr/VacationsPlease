package dev.cervantvfr.vacationsplease.domain.repository;

import dev.cervantvfr.vacationsplease.domain.model.Employee;
import dev.cervantvfr.vacationsplease.domain.model.LeaveType;
import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;
import dev.cervantvfr.vacationsplease.domain.enums.LeaveRequestStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class LeaveRequestRepositoryTest {

    @Autowired LeaveRequestRepository leaveRequestRepository;
    @Autowired TestEntityManager testEntityManager;

    @Test
    void existsOverlappingPendingOrApproved_returnsTrue_whenRangesOverlap() {
        Employee employee = new Employee("john@test.com","John","Test");
        LeaveType leaveType = new LeaveType("PTO","Paid Time Off", true);
        LeaveRequest existing = new LeaveRequest(
            employee,
            leaveType,
            LocalDate.of(2026, 6, 11),
            LocalDate.of(2026, 6, 13),
            LeaveRequestStatus.PENDING,
            "trip"
        );
        testEntityManager.persist(employee);
        testEntityManager.persist(leaveType);
        testEntityManager.persist(existing);
        
        testEntityManager.flush();
        testEntityManager.clear();
        
        boolean result = leaveRequestRepository.existsOverlappingPendingOrApproved(
            employee.getId(),
            LocalDate.of(2026, 6, 11),
            LocalDate.of(2026, 6, 13)
        );
        Assertions.assertTrue(result);
    }

    @Test
    void existsOverlappingPendingOrApproved_returnsFalse_whenNoOverlap() {
        Employee alice = new Employee("alice@test.com","Alice","Martin");
        LeaveType pto = new LeaveType("PTO","Paid Time Off", true);
        LeaveRequest existing = new LeaveRequest(
            alice,
            pto,
            LocalDate.of(2026, 6, 10),
            LocalDate.of(2026, 6, 12),
            LeaveRequestStatus.PENDING,
            "trip"
        );

        testEntityManager.persist(alice);
        testEntityManager.persist(pto);
        testEntityManager.persist(existing);
        testEntityManager.flush();
        testEntityManager.clear();

        boolean result = leaveRequestRepository.existsOverlappingPendingOrApproved(
            alice.getId(),
            LocalDate.of(2026, 6, 20),
            LocalDate.of(2026, 6, 22)
        );
        Assertions.assertFalse(result);
    }

    @Test
    void existsOverlappingPendingOrApproved_ignoredRejected() {
        Employee employee = new Employee("bob@test.com", "Bob", "Lee");
        LeaveType pto = new LeaveType("PTO", "Paid Time Off", true);
        LeaveRequest existing = new LeaveRequest(
                employee,
                pto,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 12),
                LeaveRequestStatus.PENDING,
                "old request"
        );
        
        testEntityManager.persist(employee);
        testEntityManager.persist(pto);
        testEntityManager.persist(existing);
        testEntityManager.flush();
        
        existing.setStatus(LeaveRequestStatus.REJECTED);
        testEntityManager.merge(existing);
        testEntityManager.flush();
        testEntityManager.clear();
        
        boolean result = leaveRequestRepository.existsOverlappingPendingOrApproved(
                employee.getId(),
                LocalDate.of(2026, 6, 11),
                LocalDate.of(2026, 6, 13)
        );
        Assertions.assertFalse(result);
    }
}