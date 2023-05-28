package com.bl.payrollservice;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PayRollServiceTest {
    @Test
    public void testUpdateEmployeeSalary() {
        PayrollService payrollService = new PayrollService();
        try {
            payrollService.updateEmployeeSalary("Terisa", 3000000.00);
            List<EmployeePayroll> employeePayrollList = payrollService.getEmployeePayrollData();

            for (EmployeePayroll employeePayroll : employeePayrollList) {
                if (employeePayroll.getName().equalsIgnoreCase("Terisa")) {
                    Assert.assertEquals(3000000.00, employeePayroll.getSalary());
                }
            }
        } catch (PayrollServiceException e) {
            e.printStackTrace();
        }
    }
}
