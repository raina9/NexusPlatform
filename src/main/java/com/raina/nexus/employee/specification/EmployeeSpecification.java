package com.raina.nexus.employee.specification;

import com.raina.nexus.entity.employee.Employee;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    public static Specification<Employee> hasFirstName(
            String firstName) {

        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("firstName")),
                        "%" + firstName.toLowerCase() + "%"
                );
    }
}