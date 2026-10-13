package com.ems.repository;

import com.ems.model.SalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, Long> {
    boolean existsByEmployeeIdAndMonth(Long employeeId, String month);
    List<SalaryPayment> findByEmployeeIdOrderByMonthDesc(Long employeeId);
    List<SalaryPayment> findAllByOrderByMonthDescIdDesc();
    List<SalaryPayment> findByMonth(String month);
    void deleteByEmployeeId(Long employeeId);
}
