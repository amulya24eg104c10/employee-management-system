package com.ems.controller;

import com.ems.model.Employee;
import com.ems.model.SalaryPayment;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.SalaryPaymentRepository;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final EmployeeRepository emps;
    private final DepartmentRepository depts;
    private final SalaryPaymentRepository pays;
    public DashboardController(EmployeeRepository emps, DepartmentRepository depts, SalaryPaymentRepository pays) {
        this.emps = emps; this.depts = depts; this.pays = pays;
    }

    @GetMapping
    public Map<String, Object> stats() {
        String month = YearMonth.now().toString();
        BigDecimal payroll = emps.findAll().stream().map(Employee::getBaseSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = pays.findByMonth(month).stream().map(SalaryPayment::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalEmployees", emps.count());
        m.put("totalDepartments", depts.count());
        m.put("monthlyPayroll", payroll);
        m.put("paidThisMonth", paid);
        m.put("month", month);
        return m;
    }
}
