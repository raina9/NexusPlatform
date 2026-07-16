package com.raina.nexus.employee.repository;


import com.raina.nexus.employee.dto.EmployeeSummaryResponse;
import com.raina.nexus.employee.projection.EmployeeProjection;
import com.raina.nexus.entity.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
       SELECT e.id AS id, e.firstName AS firstName, e.salary AS salary
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

    @Query("""
       SELECT e
       FROM Employee e
       WHERE e.id > :cursor
       ORDER BY e.id ASC
       """)
    List<Employee> findEmployeesCursor(
            @Param("cursor") Long cursor,
            Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM employees
                WHERE id > :afterId
                ORDER BY id ASC
                LIMIT :size
                """,
            nativeQuery = true
    )
    List<Employee> findEmployeesKeyset(
            @Param("afterId") Long afterId,
            @Param("size") int size);

    @Query("""
       SELECT e.id AS id, e.firstName AS firstName, e.salary AS salary
       FROM Employee e
       """)
    Page<EmployeeProjection> findEmployeeProjectionPage(Pageable pageable);

    @Query("""
       SELECT e.id AS id, e.firstName AS firstName, e.salary AS salary
       FROM Employee e
       WHERE e.id > :cursor
       ORDER BY e.id ASC
       """)
    List<EmployeeProjection> findEmployeeProjectionCursor(
            @Param("cursor") Long cursor,
            Pageable pageable);

    @Query(
            value = """
                SELECT id, first_name AS firstName, salary
                FROM employees
                WHERE id > :afterId
                ORDER BY id ASC
                LIMIT :size
                """,
            nativeQuery = true
    )
    List<EmployeeProjection> findEmployeeProjectionKeyset(
            @Param("afterId") Long afterId,
            @Param("size") int size);

    @Query("""
       SELECT new
       com.raina.nexus.employee.dto.EmployeeSummaryResponse(
            e.id,
            e.firstName,
            e.salary
       )
       FROM Employee e
       """)
    Page<EmployeeSummaryResponse> findEmployeeSummaryPage(Pageable pageable);

    @Query("""
       SELECT new
       com.raina.nexus.employee.dto.EmployeeSummaryResponse(
            e.id,
            e.firstName,
            e.salary
       )
       FROM Employee e
       WHERE e.id > :cursor
       ORDER BY e.id ASC
       """)
    List<EmployeeSummaryResponse> findEmployeeSummaryCursor(
            @Param("cursor") Long cursor,
            Pageable pageable);
}