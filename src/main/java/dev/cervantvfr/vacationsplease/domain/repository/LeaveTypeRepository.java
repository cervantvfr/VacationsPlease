package dev.cervantvfr.vacationsplease.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.cervantvfr.vacationsplease.domain.model.LeaveType;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    boolean existsByCode(String code);
    Optional<LeaveType> findByCode(String code);
}