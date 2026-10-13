package com.ems.controller;

import com.ems.model.Department;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentRepository repo;
    private final EmployeeRepository emps;
    public DepartmentController(DepartmentRepository repo, EmployeeRepository emps) { this.repo = repo; this.emps = emps; }

    @GetMapping public List<Department> all() { return repo.findAll(); }

    @PostMapping public Department create(@RequestBody Department d) { d.setId(null); return repo.save(d); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (emps.countByDepartmentId(id) > 0) throw new RuntimeException("Department still has employees assigned");
        repo.deleteById(id);
    }
}
