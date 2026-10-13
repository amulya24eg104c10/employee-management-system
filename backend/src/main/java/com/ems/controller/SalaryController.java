package com.ems.controller;

import com.ems.model.Employee;
import com.ems.model.SalaryPayment;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.SalaryPaymentRepository;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salaries")
public class SalaryController {
    private final SalaryPaymentRepository pays;
    private final EmployeeRepository emps;
    public SalaryController(SalaryPaymentRepository pays, EmployeeRepository emps) { this.pays = pays; this.emps = emps; }

    @GetMapping public List<SalaryPayment> all() { return pays.findAllByOrderByMonthDescIdDesc(); }

    @GetMapping("/employee/{id}") public List<SalaryPayment> byEmployee(@PathVariable Long id) { return pays.findByEmployeeIdOrderByMonthDesc(id); }

    // PAY salary: { employeeId, month (YYYY-MM, optional), bonus, deductions }
    @PostMapping("/pay")
    public SalaryPayment pay(@RequestBody Map<String, Object> b) {
        Employee e = emps.findById(Long.valueOf(b.get("employeeId").toString()))
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        String month = (b.get("month") == null || b.get("month").toString().isBlank())
                ? YearMonth.now().toString() : b.get("month").toString();
        if (pays.existsByEmployeeIdAndMonth(e.getId(), month))
            throw new RuntimeException("Salary for " + month + " already paid to " + e.getName());
        BigDecimal bonus = num(b.get("bonus")), ded = num(b.get("deductions"));
        SalaryPayment p = new SalaryPayment();
        p.setEmployee(e); p.setMonth(month);
        p.setBaseSalary(e.getBaseSalary()); p.setBonus(bonus); p.setDeductions(ded);
        p.setNetAmount(e.getBaseSalary().add(bonus).subtract(ded));
        p.setPaidDate(LocalDate.now());
        return pays.save(p);
    }

    private BigDecimal num(Object o) {
        return (o == null || o.toString().isBlank()) ? BigDecimal.ZERO : new BigDecimal(o.toString());
    }
}
