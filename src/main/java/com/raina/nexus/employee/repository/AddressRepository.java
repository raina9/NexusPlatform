package com.raina.nexus.employee.repository;

import com.raina.nexus.entity.employee.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository
        extends JpaRepository<Address, Long> {

    List<Address> findByEmployeeId(Long employeeId);

    Optional<Address> findByIdAndEmployeeId(Long id, Long employeeId);
}