package com.raina.nexus.department.controller;

import tools.jackson.databind.ObjectMapper;
import com.raina.nexus.department.dto.DepartmentRequest;
import com.raina.nexus.department.dto.DepartmentResponse;
import com.raina.nexus.department.service.DepartmentService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
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
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.departmentName")
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
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.departmentName")
                        .value("Engineering"));
    }

    @Test
    void shouldReturnDepartmentViaWebClient() throws Exception {

        DepartmentResponse response =
                new DepartmentResponse(
                        1L,
                        "Engineering"
                );

        when(departmentService.getDepartmentViaWebClient(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/departments/webclient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.departmentName")
                        .value("Engineering"));
    }
}