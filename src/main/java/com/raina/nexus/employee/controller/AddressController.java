package com.raina.nexus.employee.controller;

import com.raina.nexus.common.response.ApiResponse;
import com.raina.nexus.employee.dto.AddressRequest;
import com.raina.nexus.employee.dto.AddressResponse;
import com.raina.nexus.employee.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @PathVariable Long employeeId,
            @Valid @RequestBody AddressRequest request) {

        AddressResponse response =
                addressService.createAddress(employeeId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Address created successfully",
                        response
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Addresses fetched successfully",
                        addressService.getAddresses(employeeId)
                )
        );
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(
            @PathVariable Long employeeId,
            @PathVariable Long addressId) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Address fetched successfully",
                        addressService.getAddress(employeeId, addressId)
                )
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long employeeId,
            @PathVariable Long addressId) {

        addressService.deleteAddress(employeeId, addressId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Address deleted successfully",
                        null
                )
        );
    }
}
