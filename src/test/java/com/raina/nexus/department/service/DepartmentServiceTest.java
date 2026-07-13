package com.raina.nexus.department.service;

import com.raina.nexus.department.dto.DepartmentRequest;
import com.raina.nexus.department.dto.DepartmentResponse;
import com.raina.nexus.department.repository.DepartmentRepository;
import com.raina.nexus.entity.department.Department;
import com.raina.nexus.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void shouldCreateDepartment() {

        DepartmentRequest request =
                new DepartmentRequest("Engineering");

        Department savedDepartment =
                new Department(
                        1L,
                        "Engineering"
                );

        when(departmentRepository.save(any(Department.class)))
                .thenReturn(savedDepartment);

        DepartmentResponse response =
                departmentService.createDepartment(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Engineering", response.departmentName());

        verify(departmentRepository, times(1))
                .save(any(Department.class));
    }

    @Test
    void shouldReturnDepartmentById() {

        Department department =
                new Department(
                        1L,
                        "Engineering"
                );

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        DepartmentResponse response =
                departmentService.getDepartmentById(1L);

        assertEquals(1L, response.id());
        assertEquals("Engineering", response.departmentName());

        verify(departmentRepository)
                .findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDepartmentNotFound() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.getDepartmentById(99L)
        );

        verify(departmentRepository)
                .findById(99L);
    }
}