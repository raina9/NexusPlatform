package com.raina.nexus.department.repository;

import com.raina.nexus.entity.department.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {
}