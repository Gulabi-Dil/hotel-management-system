package dao;

import java.sql.*;
import models.Customer;
import java.util.List;
import java.util.ArrayList;
import utils.DBConnection;

public class customerDao {
    public List<Customer> getCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customer WHERE is_active = TRUE";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getString("aadhar_num"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("state"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getBoolean("is_active")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public String addCustomer(Customer customer) throws SQLException {
        String checkQuery = "SELECT is_active FROM Customer WHERE aadhar_num = ?";
        String insertQuery = "INSERT INTO Customer (aadhar_num, name, gender, phone_number, email, state, city, street, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE)";
        String updateQuery = "UPDATE Customer SET name = ?, gender = ?, phone_number = ?, email = ?, state = ?, city = ?, street = ?, is_active = TRUE WHERE aadhar_num = ?";

        String result;
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, customer.getAadhar());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    boolean active = rs.getBoolean("is_active");
                    if (!active) {
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, customer.getName());
                            updateStmt.setString(2, customer.getGender());
                            updateStmt.setString(3, customer.getPhone());
                            updateStmt.setString(4, customer.getEmail());
                            updateStmt.setString(5, customer.getState());
                            updateStmt.setString(6, customer.getCity());
                            updateStmt.setString(7, customer.getStreet());
                            updateStmt.setString(8, customer.getAadhar());
                            updateStmt.executeUpdate();
                        }
                        result = "Customer reactivated successfully!";
                    } else {
                        throw new SQLException("Customer already exists and is active.", "23505");
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, customer.getAadhar());
                        insertStmt.setString(2, customer.getName());
                        insertStmt.setString(3, customer.getGender());
                        insertStmt.setString(4, customer.getPhone());
                        insertStmt.setString(5, customer.getEmail());
                        insertStmt.setString(6, customer.getState());
                        insertStmt.setString(7, customer.getCity());
                        insertStmt.setString(8, customer.getStreet());
                        insertStmt.executeUpdate();
                    }
                    result = "success";

                }
            }
            conn.commit();
            return result;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void updateCustomer(Customer customer, String oldAadhar) throws SQLException {
        String query = "UPDATE customer SET aadhar_num = ?, name = ?, gender = ?, phone_number = ?, email = ?, state = ?, city = ?, street = ? "
                + "WHERE aadhar_num = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            stmt.setString(1, customer.getAadhar());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getGender());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getEmail());
            stmt.setString(6, customer.getState());
            stmt.setString(7, customer.getCity());
            stmt.setString(8, customer.getStreet());
            stmt.setString(9, oldAadhar);
            stmt.executeUpdate();
        }
    }

    public void softDeleteCustomer(String aadharNum) throws SQLException {
        String query = "UPDATE Customer SET is_active = FALSE WHERE aadhar_num = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, aadharNum);
            stmt.executeUpdate();
        }
    }

    public List<Customer> getDeletedCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM Customer WHERE is_active = FALSE";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getString("aadhar_num"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("state"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getBoolean("is_active")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

}
