package dev.cervantvfr.vacationsplease.domain.repository;

import dev.cervantvfr.vacationsplease.domain.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
}