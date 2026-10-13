package com.ems.config;

import com.ems.model.Admin;
import com.ems.model.Department;
import com.ems.repository.AdminRepository;
import com.ems.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final AdminRepository admins;
    private final DepartmentRepository depts;
    private final String adminUser;
    private final String adminPass;

    public DataSeeder(AdminRepository admins, DepartmentRepository depts,
                      @Value("${app.admin.username}") String adminUser,
                      @Value("${app.admin.password}") String adminPass) {
        this.admins = admins; this.depts = depts; this.adminUser = adminUser; this.adminPass = adminPass;
    }

    @Override
    public void run(String... args) {
        if (admins.findByUsername(adminUser).isEmpty()) {
            Admin a = new Admin();
            a.setUsername(adminUser);
            a.setPassword(new BCryptPasswordEncoder().encode(adminPass));
            admins.save(a);
        }
        if (depts.count() == 0) {
            for (String n : new String[]{"Engineering", "HR", "Finance", "Sales"}) {
                Department d = new Department(); d.setName(n); depts.save(d);
            }
        }
    }
}
