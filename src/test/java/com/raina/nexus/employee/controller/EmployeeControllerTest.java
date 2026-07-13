package com.raina.nexus.employee.controller;

import tools.jackson.databind.ObjectMapper;
import com.raina.nexus.common.response.CursorPageResponse;
import com.raina.nexus.employee.dto.EmployeeRequest;
import com.raina.nexus.employee.dto.EmployeeResponse;
import com.raina.nexus.employee.dto.EmployeeSummaryResponse;
import com.raina.nexus.employee.projection.EmployeeProjection;
import com.raina.nexus.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private record EmployeeProjectionView(
            Long id,
            String firstName,
            Double salary
    ) implements EmployeeProjection {

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public Double getSalary() {
            return salary;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
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
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("Shivendra"));
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
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email")
                        .value("shivendra@gmail.com"));
    }

    @Test
    void shouldReturnEmployeesByNativeSalaryQuery() throws Exception {

        EmployeeResponse response = new EmployeeResponse(
                1L, "Shivendra", "Raina", "shivendra@gmail.com", 100000.0
        );

        when(employeeService.getEmployeesWithSalaryGreaterThanNative(50000.0))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/employees/native")
                        .param("salary", "50000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void shouldReturnEmployeesCursorPage() throws Exception {

        EmployeeResponse response = new EmployeeResponse(
                2L, "Shivendra", "Raina", "shivendra@gmail.com", 100000.0
        );

        CursorPageResponse<EmployeeResponse> cursorPage =
                new CursorPageResponse<>(List.of(response), 2L, false);

        when(employeeService.getEmployeesCursor(anyLong(), anyInt()))
                .thenReturn(cursorPage);

        mockMvc.perform(get("/api/employees/cursor")
                        .param("cursor", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(2))
                .andExpect(jsonPath("$.data.nextCursor").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void shouldReturnEmployeesKeysetPage() throws Exception {

        EmployeeResponse response = new EmployeeResponse(
                2L, "Shivendra", "Raina", "shivendra@gmail.com", 100000.0
        );

        CursorPageResponse<EmployeeResponse> keysetPage =
                new CursorPageResponse<>(List.of(response), 2L, false);

        when(employeeService.getEmployeesKeyset(anyLong(), anyInt()))
                .thenReturn(keysetPage);

        mockMvc.perform(get("/api/employees/keyset")
                        .param("afterId", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(2))
                .andExpect(jsonPath("$.data.nextCursor").value(2));
    }

    @Test
    void shouldReturnInterfaceProjectionPage() throws Exception {

        EmployeeProjection projection =
                new EmployeeProjectionView(1L, "Shivendra", 100000.0);

        Page<EmployeeProjection> page =
                new PageImpl<>(List.of(projection), PageRequest.of(0, 10), 1);

        when(employeeService.getEmployeeProjections(any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/employees/projection/interface")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Shivendra"));
    }

    @Test
    void shouldReturnInterfaceProjectionCursorPage() throws Exception {

        EmployeeProjection projection =
                new EmployeeProjectionView(2L, "Shivendra", 100000.0);

        CursorPageResponse<EmployeeProjection> cursorPage =
                new CursorPageResponse<>(List.of(projection), 2L, false);

        when(employeeService.getEmployeeProjectionsCursor(anyLong(), anyInt()))
                .thenReturn(cursorPage);

        mockMvc.perform(get("/api/employees/projection/interface/cursor")
                        .param("cursor", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Shivendra"));
    }

    @Test
    void shouldReturnInterfaceProjectionKeysetPage() throws Exception {

        EmployeeProjection projection =
                new EmployeeProjectionView(2L, "Shivendra", 100000.0);

        CursorPageResponse<EmployeeProjection> keysetPage =
                new CursorPageResponse<>(List.of(projection), 2L, false);

        when(employeeService.getEmployeeProjectionsKeyset(anyLong(), anyInt()))
                .thenReturn(keysetPage);

        mockMvc.perform(get("/api/employees/projection/interface/keyset")
                        .param("afterId", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Shivendra"));
    }

    @Test
    void shouldReturnDtoProjectionPage() throws Exception {

        EmployeeSummaryResponse summary =
                new EmployeeSummaryResponse(1L, "Shivendra", 100000.0);

        Page<EmployeeSummaryResponse> page =
                new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);

        when(employeeService.getEmployeeSummaries(any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/employees/projection/dto")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Shivendra"));
    }

    @Test
    void shouldReturnDtoProjectionCursorPage() throws Exception {

        EmployeeSummaryResponse summary =
                new EmployeeSummaryResponse(2L, "Shivendra", 100000.0);

        CursorPageResponse<EmployeeSummaryResponse> cursorPage =
                new CursorPageResponse<>(List.of(summary), 2L, false);

        when(employeeService.getEmployeeSummariesCursor(anyLong(), anyInt()))
                .thenReturn(cursorPage);

        mockMvc.perform(get("/api/employees/projection/dto/cursor")
                        .param("cursor", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Shivendra"));
    }

    @Test
    void shouldReturnDtoProjectionKeysetPage() throws Exception {

        EmployeeSummaryResponse summary =
                new EmployeeSummaryResponse(2L, "Shivendra", 100000.0);

        CursorPageResponse<EmployeeSummaryResponse> keysetPage =
                new CursorPageResponse<>(List.of(summary), 2L, false);

        when(employeeService.getEmployeeSummariesKeyset(anyLong(), anyInt()))
                .thenReturn(keysetPage);

        mockMvc.perform(get("/api/employees/projection/dto/keyset")
                        .param("afterId", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Shivendra"));
    }
}