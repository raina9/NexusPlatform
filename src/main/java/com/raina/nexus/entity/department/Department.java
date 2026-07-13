package com.raina.nexus.entity.department;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "department_name",
            nullable = false,
            unique = true
    )
    private String departmentName;
}