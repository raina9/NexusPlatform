package com.raina.nexus.employee.service;

import com.raina.nexus.common.response.CursorPageResponse;
import com.raina.nexus.employee.dto.EmployeeRequest;
import com.raina.nexus.employee.dto.EmployeeResponse;
import com.raina.nexus.employee.dto.EmployeeSummaryResponse;
import com.raina.nexus.employee.projection.EmployeeProjection;
import com.raina.nexus.employee.repository.EmployeeRepository;
import com.raina.nexus.entity.employee.Employee;
import com.raina.nexus.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    /**
     * Get Employees With Salary Greater Than (Native Query)
     */
    public List<EmployeeResponse> getEmployeesWithSalaryGreaterThanNative(
            Double salary) {

        log.info(
                "Fetching employees via native query salary greater than={}",
                salary
        );

        return employeeRepository
                .findEmployeesNative(salary)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get Employees - Cursor Pagination
     */
    public CursorPageResponse<EmployeeResponse> getEmployeesCursor(
            Long cursor, int size) {

        log.info(
                "Fetching employees cursor pagination cursor={}, size={}",
                cursor,
                size
        );

        List<Employee> rows = employeeRepository.findEmployeesCursor(
                cursor,
                PageRequest.of(0, size + 1)
        );

        return buildCursorPage(
                rows.stream().map(this::mapToResponse).toList(),
                size,
                EmployeeResponse::id
        );
    }

    /**
     * Get Employees - Keyset Pagination (Native Query)
     */
    public CursorPageResponse<EmployeeResponse> getEmployeesKeyset(
            Long afterId, int size) {

        log.info(
                "Fetching employees keyset pagination afterId={}, size={}",
                afterId,
                size
        );

        List<Employee> rows = employeeRepository.findEmployeesKeyset(
                afterId,
                size + 1
        );

        return buildCursorPage(
                rows.stream().map(this::mapToResponse).toList(),
                size,
                EmployeeResponse::id
        );
    }

    /**
     * Get Employee Interface Projections - Offset Pagination
     */
    public Page<EmployeeProjection> getEmployeeProjections(Pageable pageable) {

        log.info(
                "Fetching employee interface projections page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return employeeRepository.findEmployeeProjectionPage(pageable);
    }

    /**
     * Get Employee Interface Projections - Cursor Pagination
     */
    public CursorPageResponse<EmployeeProjection> getEmployeeProjectionsCursor(
            Long cursor, int size) {

        log.info(
                "Fetching employee interface projections cursor pagination cursor={}, size={}",
                cursor,
                size
        );

        List<EmployeeProjection> rows = employeeRepository
                .findEmployeeProjectionCursor(cursor, PageRequest.of(0, size + 1));

        return buildCursorPage(rows, size, EmployeeProjection::getId);
    }

    /**
     * Get Employee Interface Projections - Keyset Pagination (Native Query)
     */
    public CursorPageResponse<EmployeeProjection> getEmployeeProjectionsKeyset(
            Long afterId, int size) {

        log.info(
                "Fetching employee interface projections keyset pagination afterId={}, size={}",
                afterId,
                size
        );

        List<EmployeeProjection> rows = employeeRepository
                .findEmployeeProjectionKeyset(afterId, size + 1);

        return buildCursorPage(rows, size, EmployeeProjection::getId);
    }

    /**
     * Get Employee DTO Projections - Offset Pagination
     */
    public Page<EmployeeSummaryResponse> getEmployeeSummaries(Pageable pageable) {

        log.info(
                "Fetching employee DTO projections page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return employeeRepository.findEmployeeSummaryPage(pageable);
    }

    /**
     * Get Employee DTO Projections - Cursor Pagination
     */
    public CursorPageResponse<EmployeeSummaryResponse> getEmployeeSummariesCursor(
            Long cursor, int size) {

        log.info(
                "Fetching employee DTO projections cursor pagination cursor={}, size={}",
                cursor,
                size
        );

        List<EmployeeSummaryResponse> rows = employeeRepository
                .findEmployeeSummaryCursor(cursor, PageRequest.of(0, size + 1));

        return buildCursorPage(rows, size, EmployeeSummaryResponse::id);
    }

    /**
     * Get Employee DTO Projections - Keyset Pagination (Native Query)
     *
     * The native keyset query returns full Employee rows since JPQL
     * constructor expressions ("SELECT new ...") are not supported in
     * native SQL - results are mapped to the DTO after fetch.
     */
    public CursorPageResponse<EmployeeSummaryResponse> getEmployeeSummariesKeyset(
            Long afterId, int size) {

        log.info(
                "Fetching employee DTO projections keyset pagination afterId={}, size={}",
                afterId,
                size
        );

        List<Employee> rows = employeeRepository.findEmployeesKeyset(
                afterId,
                size + 1
        );

        List<EmployeeSummaryResponse> summaries = rows.stream()
                .map(e -> new EmployeeSummaryResponse(
                        e.getId(),
                        e.getFirstName(),
                        e.getSalary()
                ))
                .toList();

        return buildCursorPage(summaries, size, EmployeeSummaryResponse::id);
    }

    /**
     * Builds a CursorPageResponse from a size+1 fetched window
     */
    private <T> CursorPageResponse<T> buildCursorPage(
            List<T> rows,
            int size,
            java.util.function.Function<T, Long> idExtractor) {

        boolean hasNext = rows.size() > size;

        List<T> content = hasNext
                ? rows.subList(0, size)
                : rows;

        Long nextCursor = content.isEmpty()
                ? null
                : idExtractor.apply(content.get(content.size() - 1));

        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }
}