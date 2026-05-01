package dev.cervantvfr.vacationsplease.domain.repository;

import dev.cervantvfr.vacationsplease.domain.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
}