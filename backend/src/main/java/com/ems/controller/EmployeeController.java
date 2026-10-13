package com.ems.controller;

import com.ems.model.Department;
import com.ems.model.Employee;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.SalaryPaymentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeRepository repo;
    private final DepartmentRepository depts;
    private final SalaryPaymentRepository pays;
    public EmployeeController(EmployeeRepository repo, DepartmentRepository depts, SalaryPaymentRepository pays) {
        this.repo = repo; this.depts = depts; this.pays = pays;
    }

    private Employee find(Long id) { return repo.findById(id).orElseThrow(() -> new RuntimeException("Employee not found")); }

    private Department dept(Object id) {
        if (id == null || id.toString().isBlank()) return null;
        return depts.findById(Long.valueOf(id.toString())).orElseThrow(() -> new RuntimeException("Department not found"));
    }

    @GetMapping public List<Employee> all() { return repo.findAll(); }

    @GetMapping("/{id}") public Employee one(@PathVariable Long id) { return find(id); }

    // ADD employee
    @PostMapping
    public Employee create(@RequestBody Employee e) {
        e.setId(null);
        e.setDepartment(e.getDepartment() == null ? null : dept(e.getDepartment().getId()));
        if (e.getJoiningDate() == null) e.setJoiningDate(LocalDate.now());
        if (e.getBaseSalary() == null) e.setBaseSalary(BigDecimal.ZERO);
        return repo.save(e);
    }

    // EDIT details
    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody Employee in) {
        Employee e = find(id);
        e.setName(in.getName()); e.setEmail(in.getEmail()); e.setPhone(in.getPhone());
        e.setDesignation(in.getDesignation());
        if (in.getJoiningDate() != null) e.setJoiningDate(in.getJoiningDate());
        return repo.save(e);
    }

    // ASSIGN department / designation
    @PutMapping("/{id}/assign")
    public Employee assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Employee e = find(id);
        e.setDepartment(dept(body.get("departmentId")));
        if (body.get("designation") != null) e.setDesignation(body.get("designation").toString());
        return repo.save(e);
    }

    // SET salary
    @PutMapping("/{id}/salary")
    public Employee setSalary(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Employee e = find(id);
        BigDecimal s = new BigDecimal(body.get("baseSalary").toString());
        if (s.signum() < 0) throw new RuntimeException("Salary cannot be negative");
        e.setBaseSalary(s);
        return repo.save(e);
    }

    // REMOVE employee
    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        find(id);
        pays.deleteByEmployeeId(id);
        repo.deleteById(id);
    }
}
