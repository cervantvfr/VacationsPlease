package dev.cervantvfr.vacationsplease.domain.repository;

import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;
import dev.cervantvfr.vacationsplease.domain.enums.LeaveRequestStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);
    Page<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveRequestStatus status, Pageable pageable);
    Page<LeaveRequest> findByEmployeeIdAndStatusOrderByStartDateDesc(Long employeeId, LeaveRequestStatus status, Pageable pageable);
    
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