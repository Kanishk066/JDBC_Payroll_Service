package com.bl.payrollservice;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PayrollServiceException extends Exception {
    public PayrollServiceException(String message) {
        super(message);
    }

    public PayrollServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class PayrollService {
    private static PayrollService instance;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/PayrollService?useSSL=false";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Kanishk@66";
    PayrollService() {
    }
    public static PayrollService getInstance() {
        if (instance == null) {
            instance = new PayrollService();
        }
        return instance;
    }
    private PreparedStatement preparedStatement;
    private Map<String, PreparedStatement> preparedStatementCache = new HashMap<>();

    public List<EmployeePayroll> getEmployeesByDateRange(LocalDate startDate, LocalDate endDate) throws PayrollServiceException {
        List<EmployeePayroll> employeePayrollList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String selectSql = "SELECT id, name, salary, start_date FROM employee_payroll WHERE start_date BETWEEN ? AND ?";
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double salary = resultSet.getDouble("salary");
                LocalDate joinDate = resultSet.getDate("start_date").toLocalDate();

                EmployeePayroll employeePayroll = new EmployeePayroll(id, name, salary, joinDate);
                employeePayrollList.add(employeePayroll);
            }
        } catch (SQLException e) {
            throw new PayrollServiceException("Error retrieving employee data by date range", e);
        }

        return employeePayrollList;
    }
    public void updateEmployeeSalary(String employeeName, double newSalary) throws PayrollServiceException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String updateSql = "UPDATE employee_payroll SET salary = ? WHERE name = ?";

            // Check if the PreparedStatement is already cached
            if (preparedStatementCache.containsKey(updateSql)) {
                preparedStatement = preparedStatementCache.get(updateSql);
            } else {
                preparedStatement = connection.prepareStatement(updateSql);
                preparedStatementCache.put(updateSql, preparedStatement);
            }

            preparedStatement.setDouble(1, newSalary);
            preparedStatement.setString(2, employeeName);

            int rowsUpdated = preparedStatement.executeUpdate();

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
            LocalDate startDate = LocalDate.of(2023, 5, 1);
            LocalDate endDate = LocalDate.of(2023, 5, 10);
            List<EmployeePayroll> employees = payrollService.getEmployeesByDateRange(startDate, endDate);

        } catch (PayrollServiceException e) {
            e.printStackTrace();
        }
    }
}
