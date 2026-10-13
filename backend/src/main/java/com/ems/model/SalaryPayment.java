package com.ems.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Data
@Table(name = "salary_payments")
public class SalaryPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    @Column(name = "pay_month", nullable = false)
    private String month; // format YYYY-MM
    private BigDecimal baseSalary;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal netAmount;
    private LocalDate paidDate;
}
