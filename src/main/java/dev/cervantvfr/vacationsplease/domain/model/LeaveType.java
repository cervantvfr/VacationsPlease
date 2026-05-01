package dev.cervantvfr.vacationsplease.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "leave_types")
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected LeaveType() {
        // Required by JPA
    }

    public LeaveType(String code, String name, boolean isPaid) {
        this.code = code;
        this.name = name;
        this.isPaid = isPaid;
        this.createdAt = OffsetDateTime.now();
    }
    
    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeaveType other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}