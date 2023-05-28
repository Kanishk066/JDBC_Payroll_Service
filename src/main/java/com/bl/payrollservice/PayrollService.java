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

    public double getSumOfSalariesByGender(String gender) throws PayrollServiceException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String selectSql = "SELECT SUM(salary) FROM employee_payroll WHERE gender = ? GROUP BY gender";
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setString(1, gender);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (SQLException e) {
            throw new PayrollServiceException("Error calculating sum of salaries by gender", e);
        }

        return 0;
    }

    public double getAverageSalaryByGender(String gender) throws PayrollServiceException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String selectSql = "SELECT AVG(salary) FROM employee_payroll WHERE gender = ? GROUP BY gender";
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setString(1, gender);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (SQLException e) {
            throw new PayrollServiceException("Error calculating average salary by gender", e);
        }

        return 0;
    }

    public double getMinimumSalaryByGender(String gender) throws PayrollServiceException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String selectSql = "SELECT MIN(salary) FROM employee_payroll WHERE gender = ? GROUP BY gender";
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setString(1, gender);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (SQLException e) {
            throw new PayrollServiceException("Error calculating minimum salary by gender", e);
        }

        return 0;
    }

    public double getMaximumSalaryByGender(String gender) throws PayrollServiceException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String selectSql = "SELECT MAX(salary) FROM employee_payroll WHERE gender = ? GROUP BY gender";
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setString(1, gender);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (SQLException e) {
            throw new PayrollServiceException("Error calculating maximum salary by gender", e);
        }

        return 0;
    }

    public int getEmployeeCountByGender(String gender) throws PayrollServiceException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String selectSql = "SELECT COUNT(*) FROM employee_payroll WHERE gender = ? GROUP BY gender";
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setString(1, gender);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new PayrollServiceException("Error calculating employee count by gender", e);
        }

        return 0;
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
        String gender = "M"; // Gender for which to retrieve salary statistics

        try {
            // Retrieve and print the sum of salaries by gender
            double sumOfSalaries = payrollService.getSumOfSalariesByGender(gender);
            System.out.println("Sum of salaries for gender " + gender + ": " + sumOfSalaries);

            // Retrieve and print the average salary by gender
            double averageSalary = payrollService.getAverageSalaryByGender(gender);
            System.out.println("Average salary for gender " + gender + ": " + averageSalary);

            // Retrieve and print the minimum salary by gender
            double minimumSalary = payrollService.getMinimumSalaryByGender(gender);
            System.out.println("Minimum salary for gender " + gender + ": " + minimumSalary);

            // Retrieve and print the maximum salary by gender
            double maximumSalary = payrollService.getMaximumSalaryByGender(gender);
            System.out.println("Maximum salary for gender " + gender + ": " + maximumSalary);

            // Retrieve and print the count of employees by gender
            int employeeCount = payrollService.getEmployeeCountByGender(gender);
            System.out.println("Employee count for gender " + gender + ": " + employeeCount);
        } catch (PayrollServiceException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
