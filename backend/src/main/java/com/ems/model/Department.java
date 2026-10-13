package com.ems.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity @Data
@Table(name = "departments")
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
}
