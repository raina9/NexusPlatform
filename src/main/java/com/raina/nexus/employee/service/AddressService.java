package com.raina.nexus.employee.service;

import com.raina.nexus.employee.dto.AddressRequest;
import com.raina.nexus.employee.dto.AddressResponse;
import com.raina.nexus.employee.repository.AddressRepository;
import com.raina.nexus.employee.repository.EmployeeRepository;
import com.raina.nexus.entity.employee.Address;
import com.raina.nexus.entity.employee.Employee;
import com.raina.nexus.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Create Address For Employee
     */
    public AddressResponse createAddress(Long employeeId, AddressRequest request) {

        log.info(
                "Creating address for employeeId={}",
                employeeId
        );

        Employee employee = getEmployeeOrThrow(employeeId);

        Address address = Address.builder()
                .city(request.city())
                .state(request.state())
                .country(request.country())
                .employee(employee)
                .build();

        Address savedAddress = addressRepository.save(address);

        log.info(
                "Address created successfully with id={} for employeeId={}",
                savedAddress.getId(),
                employeeId
        );

        return mapToResponse(savedAddress);
    }

    /**
     * Get All Addresses For Employee
     */
    public List<AddressResponse> getAddresses(Long employeeId) {

        log.info(
                "Fetching addresses for employeeId={}",
                employeeId
        );

        getEmployeeOrThrow(employeeId);

        return addressRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get Address By Id For Employee
     */
    public AddressResponse getAddress(Long employeeId, Long addressId) {

        log.info(
                "Fetching address with id={} for employeeId={}",
                addressId,
                employeeId
        );

        getEmployeeOrThrow(employeeId);

        Address address = getAddressOrThrow(employeeId, addressId);

        return mapToResponse(address);
    }

    /**
     * Delete Address For Employee
     */
    public void deleteAddress(Long employeeId, Long addressId) {

        log.warn(
                "Deleting address with id={} for employeeId={}",
                addressId,
                employeeId
        );

        getEmployeeOrThrow(employeeId);

        Address address = getAddressOrThrow(employeeId, addressId);

        addressRepository.delete(address);

        log.info(
                "Address deleted successfully with id={} for employeeId={}",
                addressId,
                employeeId
        );
    }

    private Employee getEmployeeOrThrow(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> {

                    log.error(
                            "EMPLOYEE_NOT_FOUND: employee not found with id={}",
                            employeeId
                    );

                    return new ResourceNotFoundException(
                            "EMPLOYEE_NOT_FOUND: Employee not found with id : " + employeeId
                    );
                });
    }

    private Address getAddressOrThrow(Long employeeId, Long addressId) {

        return addressRepository.findByIdAndEmployeeId(addressId, employeeId)
                .orElseThrow(() -> {

                    log.error(
                            "ADDRESS_NOT_FOUND: address not found with id={} for employeeId={}",
                            addressId,
                            employeeId
                    );

                    return new ResourceNotFoundException(
                            "ADDRESS_NOT_FOUND: Address not found with id : " + addressId
                    );
                });
    }

    /**
     * Entity -> Response DTO Mapper
     */
    private AddressResponse mapToResponse(Address address) {

        return new AddressResponse(
                address.getId(),
                address.getCity(),
                address.getState(),
                address.getCountry()
        );
    }
}
