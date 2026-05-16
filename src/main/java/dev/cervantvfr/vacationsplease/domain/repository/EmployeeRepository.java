package dev.cervantvfr.vacationsplease.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.cervantvfr.vacationsplease.domain.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
    Optional<Employee> findByEmail(String email);
}