package com.raina.nexus.employee.repository;

import com.raina.nexus.entity.employee.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository
        extends JpaRepository<Address, Long> {
}