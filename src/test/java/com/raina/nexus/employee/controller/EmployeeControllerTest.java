package com.raina.nexus.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raina.nexus.employee.dto.EmployeeRequest;
import com.raina.nexus.employee.dto.EmployeeResponse;
import com.raina.nexus.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void shouldCreateEmployee() throws Exception {

        EmployeeRequest request =
                new EmployeeRequest(
                        "Shivendra",
                        "Raina",
                        "shivendra@gmail.com",
                        100000.0
                );

        EmployeeResponse response =
                new EmployeeResponse(
                        1L,
                        "Shivendra",
                        "Raina",
                        "shivendra@gmail.com",
                        100000.0
                );

        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/employees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Shivendra"));
    }

    @Test
    void shouldReturnEmployeeById() throws Exception {

        EmployeeResponse response =
                new EmployeeResponse(
                        1L,
                        "Shivendra",
                        "Raina",
                        "shivendra@gmail.com",
                        100000.0
                );

        when(employeeService.getEmployeeById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("shivendra@gmail.com"));
    }
}