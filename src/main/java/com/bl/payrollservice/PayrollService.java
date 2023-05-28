package com.bl.payrollservice;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class PayrollServiceException extends Exception {
    public PayrollServiceException(String message) {
        super(message);
    }

    public PayrollServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class PayrollService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/PayrollService?useSSL=false";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Kanishk@66";

    public List<EmployeePayroll> getEmployeePayrollData() throws PayrollServiceException {
        List<EmployeePayroll> employeePayrollList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String selectSql = "SELECT id, name, salary, start_date FROM employee_payroll";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double salary = resultSet.getDouble("salary");
                LocalDate startDate = resultSet.getDate("start_date").toLocalDate();

                EmployeePayroll employeePayroll = new EmployeePayroll(id, name, salary, startDate);
                employeePayrollList.add(employeePayroll);

                // Update the salary for Terisa if her name matches
                if (name.equalsIgnoreCase("Terisa")) {
                    double newSalary = 3000000.00;
                    employeePayroll.setSalary(newSalary);
                    updateEmployeeSalary(name, newSalary);
                }
            }

        } catch (SQLException e) {
            throw new PayrollServiceException("Error retrieving employee payroll data", e);
        }

        return employeePayrollList;
    }
    public void updateEmployeeSalary(String employeeName, double newSalary) throws PayrollServiceException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "UPDATE employee_payroll SET salary = ? WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, newSalary);
            statement.setString(2, employeeName);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new PayrollServiceException("Employee not found: " + employeeName);
            }
        } catch (SQLException e) {
            throw new PayrollServiceException("Error updating employee salary", e);
        }
    }
    public static void main(String[] args) {
        PayrollService payrollService = new PayrollService();
        try {
            List<EmployeePayroll> employeePayrollList = payrollService.getEmployeePayrollData();
            for (EmployeePayroll employeePayroll : employeePayrollList) {
                System.out.println(employeePayroll);
            }
        } catch (PayrollServiceException e) {
            e.printStackTrace();
        }
    }
}
