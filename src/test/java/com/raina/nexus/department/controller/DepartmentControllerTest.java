package com.raina.nexus.department.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raina.nexus.department.dto.DepartmentRequest;
import com.raina.nexus.department.dto.DepartmentResponse;
import com.raina.nexus.department.service.DepartmentService;
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

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    @Test
    void shouldCreateDepartment() throws Exception {

        DepartmentRequest request =
                new DepartmentRequest("Engineering");

        DepartmentResponse response =
                new DepartmentResponse(
                        1L,
                        "Engineering"
                );

        when(departmentService.createDepartment(any(DepartmentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/departments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.departmentName")
                        .value("Engineering"));
    }

    @Test
    void shouldReturnDepartmentById() throws Exception {

        DepartmentResponse response =
                new DepartmentResponse(
                        1L,
                        "Engineering"
                );

        when(departmentService.getDepartmentById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.departmentName")
                        .value("Engineering"));
    }
}