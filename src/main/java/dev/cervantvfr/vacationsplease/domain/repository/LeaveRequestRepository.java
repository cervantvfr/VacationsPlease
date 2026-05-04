package dev.cervantvfr.vacationsplease.domain.repository;

import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;
import dev.cervantvfr.vacationsplease.domain.enums.LeaveRequestStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<LeaveRequest> findByEmployeeIdAndStatusOrderByStartDateDesc(Long employeeId, LeaveRequestStatus status);

    @Query("""
    select count(lr) > 0
    from LeaveRequest lr
    where lr.employee.id = :employeeId
    and lr.status in :statuses
    and lr.startDate <= :endDate
    and lr.endDate >= :startDate
    """)

    boolean existsOverlappingRequest(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("statuses") Collection<LeaveRequestStatus> statuses
    );

    default boolean existsOverlappingPendingOrApproved(
        Long employeeId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return existsOverlappingRequest(
            employeeId,
            startDate,
            endDate,
            List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED)
        );
    }
}