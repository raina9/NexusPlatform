package com.raina.nexus.employee.service;

import com.raina.nexus.employee.dto.EmployeeRequest;
import com.raina.nexus.employee.dto.EmployeeResponse;
import com.raina.nexus.employee.repository.EmployeeRepository;
import com.raina.nexus.entity.employee.Employee;
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
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void shouldCreateEmployee() {

        EmployeeRequest request = new EmployeeRequest(
                "Shivendra",
                "Raina",
                "shivendra@gmail.com",
                100000.0
        );

        Employee savedEmployee = Employee.builder()
                .id(1L)
                .firstName("Shivendra")
                .lastName("Raina")
                .email("shivendra@gmail.com")
                .salary(100000.0)
                .departmentId(1L)
                .build();

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(savedEmployee);

        EmployeeResponse response =
                employeeService.createEmployee(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Shivendra", response.firstName());

        verify(employeeRepository, times(1))
                .save(any(Employee.class));
    }

    @Test
    void shouldReturnEmployeeById() {

        Employee employee = Employee.builder()
                .id(1L)
                .firstName("Shivendra")
                .lastName("Raina")
                .email("shivendra@gmail.com")
                .salary(100000.0)
                .departmentId(1L)
                .build();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        EmployeeResponse response =
                employeeService.getEmployeeById(1L);

        assertEquals(1L, response.id());
        assertEquals("Shivendra", response.firstName());

        verify(employeeRepository)
                .findById(1L);
    }
    @Test
    void shouldThrowExceptionWhenEmployeeNotFound() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(99L)
        );

        verify(employeeRepository)
                .findById(99L);
    }
}