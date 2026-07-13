package com.raina.nexus.employee.repository;


import com.raina.nexus.employee.dto.EmployeeSummaryResponse;
import com.raina.nexus.employee.projection.EmployeeProjection;
import com.raina.nexus.entity.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {
    List<Employee> findByFirstNameContainingIgnoreCase(
            String firstName);

    @Query("""
       SELECT e
       FROM Employee e
       WHERE e.salary > :salary
       """)
    List<Employee> findEmployeesWithSalaryGreaterThan(
            @Param("salary") Double salary);

    @Query(
            value = """
                SELECT *
                FROM employees
                WHERE salary > :salary
                """,
            nativeQuery = true
    )
    List<Employee> findEmployeesNative(
            @Param("salary") Double salary);

    @Query("""
       SELECT e
       FROM Employee e
       """)
    List<EmployeeProjection> findEmployeeProjection();

    @Query("""
       SELECT new
       com.raina.nexus.employee.dto.EmployeeSummaryResponse(
            e.id,
            e.firstName,
            e.salary
       )
       FROM Employee e
       """)
    List<EmployeeSummaryResponse> findEmployeeSummary();
}