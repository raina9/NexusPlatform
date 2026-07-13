package com.raina.nexus.employee.service;

import com.raina.nexus.employee.dto.EmployeeRequest;
import com.raina.nexus.employee.dto.EmployeeResponse;
import com.raina.nexus.employee.repository.EmployeeRepository;
import com.raina.nexus.entity.employee.Employee;
import com.raina.nexus.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.raina.nexus.employee.specification.EmployeeSpecification.hasFirstName;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * Create Employee
     */
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        log.info(
                "Creating employee with email={}",
                request.email()
        );

        Employee employee = new Employee();

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setSalary(request.salary());

        Employee savedEmployee = employeeRepository.save(employee);

        log.info(
                "Employee created successfully with id={}",
                savedEmployee.getId()
        );

        return mapToResponse(savedEmployee);
    }

    /**
     * Get Employee By Id
     */
    public EmployeeResponse getEmployeeById(Long id) {

        log.info(
                "Fetching employee with id={}",
                id
        );

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {

                    log.error(
                            "Employee not found with id={}",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Employee not found with id : " + id
                    );
                });

        return mapToResponse(employee);
    }

    /**
     * Get All Employees
     */
    public List<EmployeeResponse> getAllEmployees() {

        log.info("Fetching all employees");

        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Delete Employee
     */
    public void deleteEmployee(Long id) {

        log.warn(
                "Deleting employee with id={}",
                id
        );

        employeeRepository.findById(id)
                .orElseThrow(() -> {

                    log.error(
                            "Employee not found with id={}",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Employee not found with id : " + id
                    );
                });

        employeeRepository.deleteById(id);

        log.info(
                "Employee deleted successfully with id={}",
                id
        );
    }

    /**
     * Entity -> Response DTO Mapper
     */
    private EmployeeResponse mapToResponse(Employee employee) {

        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getSalary()
        );
    }

    /**
     * Get Employees With Pagination
     */
    public Page<EmployeeResponse> getEmployees(Pageable pageable) {

        log.info(
                "Fetching employees page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return employeeRepository
                .findAll(pageable)
                .map(this::mapToResponse);
    }

    public List<EmployeeResponse> searchEmployees(
            String firstName) {

        log.info(
                "Searching employees by firstName={}",
                firstName
        );

        return employeeRepository
                .findByFirstNameContainingIgnoreCase(firstName)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<EmployeeResponse> searchWithSpecification(
            String firstName) {

        log.info(
                "Searching employees using specification firstName={}",
                firstName
        );

        return employeeRepository
                .findAll(hasFirstName(firstName))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<EmployeeResponse> getEmployeesWithSalaryGreaterThan(
            Double salary) {

        log.info(
                "Fetching employees with salary greater than={}",
                salary
        );

        return employeeRepository
                .findEmployeesWithSalaryGreaterThan(salary)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}