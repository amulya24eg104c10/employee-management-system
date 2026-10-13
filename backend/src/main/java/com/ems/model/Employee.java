package com.ems.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Data
@Table(name = "employees")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String phone;
    private String designation;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    private LocalDate joiningDate;
    @Column(precision = 12, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;
}
