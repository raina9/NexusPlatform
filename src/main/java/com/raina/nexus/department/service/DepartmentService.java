package com.raina.nexus.department.service;

import com.raina.nexus.department.dto.DepartmentRequest;
import com.raina.nexus.department.dto.DepartmentResponse;
import com.raina.nexus.department.repository.DepartmentRepository;
import com.raina.nexus.entity.department.Department;
import com.raina.nexus.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    /**
     * Create Department
     */
    public DepartmentResponse createDepartment(DepartmentRequest request) {

        log.info(
                "Creating department={}",
                request.departmentName()
        );

        Department department = new Department();

        department.setDepartmentName(request.departmentName());

        Department savedDepartment =
                departmentRepository.save(department);

        log.info(
                "Department created successfully with id={}",
                savedDepartment.getId()
        );

        return mapToResponse(savedDepartment);
    }

    /**
     * Get Department By Id
     */
    public DepartmentResponse getDepartmentById(Long id) {

        log.info(
                "Fetching department with id={}",
                id
        );

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> {

                    log.error(
                            "Department not found with id={}",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Department not found with id : " + id
                    );
                });

        return mapToResponse(department);
    }

    /**
     * Get All Departments
     */
    public List<DepartmentResponse> getAllDepartments() {

        log.info("Fetching all departments");

        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Delete Department
     */
    public void deleteDepartment(Long id) {

        log.warn(
                "Deleting department with id={}",
                id
        );

        departmentRepository.findById(id)
                .orElseThrow(() -> {

                    log.error(
                            "Department not found with id={}",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Department not found with id : " + id
                    );
                });

        departmentRepository.deleteById(id);

        log.info(
                "Department deleted successfully with id={}",
                id
        );
    }

    /**
     * Entity -> Response DTO Mapper
     */
    private DepartmentResponse mapToResponse(
            Department department) {

        return new DepartmentResponse(
                department.getId(),
                department.getDepartmentName()
        );
    }
}