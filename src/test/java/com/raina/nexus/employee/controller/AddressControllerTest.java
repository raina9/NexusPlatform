package com.raina.nexus.employee.controller;

import tools.jackson.databind.ObjectMapper;
import com.raina.nexus.employee.dto.AddressRequest;
import com.raina.nexus.employee.dto.AddressResponse;
import com.raina.nexus.employee.service.AddressService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

    @Test
    void shouldCreateAddress() throws Exception {

        AddressRequest request =
                new AddressRequest("Pune", "Maharashtra", "India");

        AddressResponse response =
                new AddressResponse(1L, "Pune", "Maharashtra", "India");

        when(addressService.createAddress(any(Long.class), any(AddressRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/employees/10/addresses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.city").value("Pune"));
    }

    @Test
    void shouldReturnAllAddressesForEmployee() throws Exception {

        AddressResponse response =
                new AddressResponse(1L, "Pune", "Maharashtra", "India");

        when(addressService.getAddresses(10L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/employees/10/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].city").value("Pune"));
    }

    @Test
    void shouldReturnAddressById() throws Exception {

        AddressResponse response =
                new AddressResponse(1L, "Pune", "Maharashtra", "India");

        when(addressService.getAddress(10L, 1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/employees/10/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.country").value("India"));
    }

    @Test
    void shouldDeleteAddress() throws Exception {

        mockMvc.perform(delete("/api/employees/10/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(addressService).deleteAddress(10L, 1L);
    }
}
