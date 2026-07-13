package com.raina.nexus.exception;

import com.raina.nexus.department.controller.DepartmentController;
import com.raina.nexus.department.service.DepartmentService;
import com.raina.nexus.employee.controller.AddressController;
import com.raina.nexus.employee.service.AddressService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
        AddressController.class,
        DepartmentController.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    void shouldReturnNotFoundForEmployeeNotFound() throws Exception {

        when(addressService.getAddresses(99L))
                .thenThrow(new ResourceNotFoundException(
                        "EMPLOYEE_NOT_FOUND: Employee not found with id : 99"
                ));

        mockMvc.perform(get("/api/employees/99/addresses"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("EMPLOYEE_NOT_FOUND: Employee not found with id : 99"))
                .andExpect(jsonPath("$.path").value("/api/employees/99/addresses"));
    }

    @Test
    void shouldReturnNotFoundForAddressNotFound() throws Exception {

        when(addressService.getAddress(1L, 99L))
                .thenThrow(new ResourceNotFoundException(
                        "ADDRESS_NOT_FOUND: Address not found with id : 99"
                ));

        mockMvc.perform(get("/api/employees/1/addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("ADDRESS_NOT_FOUND: Address not found with id : 99"))
                .andExpect(jsonPath("$.path").value("/api/employees/1/addresses/99"));
    }

    @Test
    void shouldReturnNotFoundForDepartmentNotFound() throws Exception {

        when(departmentService.getDepartmentViaWebClient(99L))
                .thenThrow(new ResourceNotFoundException(
                        "DEPARTMENT_NOT_FOUND: Department not found with id : 99"
                ));

        mockMvc.perform(get("/api/departments/webclient/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("DEPARTMENT_NOT_FOUND: Department not found with id : 99"))
                .andExpect(jsonPath("$.path").value("/api/departments/webclient/99"));
    }
}
