package dev.cervantvfr.vacationsplease.domain.repository;

import dev.cervantvfr.vacationsplease.domain.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    boolean existsByCode(String code);
    Optional<LeaveType> findByCode(String code);
}