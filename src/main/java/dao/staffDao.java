package dao;

import java.sql.*;
import models.Staff;
import models.StaffRole;
import java.util.List;
import java.util.ArrayList;
import utils.DBConnection;

public class staffDao {
    public List<Staff> getAllStaff() {
        List<Staff> staff = new ArrayList<>();
        String query = "SELECT * FROM staff";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                staff.add(
                        new Staff(
                                rs.getInt("hotel_id"),
                                rs.getInt("staff_id"),
                                rs.getString("name"),
                                rs.getString("gender"),
                                rs.getString("phone"),
                                rs.getString("email"),
                                rs.getDate("join_date") != null ? rs.getDate("join_date").toLocalDate() : null,
                                rs.getDouble("salary"),
                                rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staff;
    }

    public List<StaffRole> getRoles(int hotelId) {
        List<StaffRole> staffRoles = new ArrayList<>();
        String query = "SELECT role, COUNT(*) AS total FROM staff WHERE hotel_id = ? AND is_active = TRUE GROUP BY role";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                staffRoles.add(
                        new StaffRole(
                                rs.getString("role"),
                                rs.getInt("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffRoles;
    }

    public List<Staff> getStaffByHotel(int hotelId) {
        List<Staff> staff = new ArrayList<>();
        String query = "SELECT * FROM staff WHERE hotel_id = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                staff.add(
                        new Staff(
                                rs.getInt("hotel_id"),
                                rs.getInt("staff_id"),
                                rs.getString("name"),
                                rs.getString("gender"),
                                rs.getString("phone"),
                                rs.getString("email"),
                                rs.getDate("join_date") != null ? rs.getDate("join_date").toLocalDate() : null,
                                rs.getDouble("salary"),
                                rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staff;
    }

    public void addStaff(Staff staff) throws SQLException {
        String query = "INSERT INTO staff(hotel_id,staff_id,name,gender,phone,email,join_date,salary,role) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            stmt.setInt(1, staff.getHotelId());
            stmt.setInt(2, staff.getStaffId());
            stmt.setString(3, staff.getName());
            stmt.setString(4, staff.getGender());
            stmt.setString(5, staff.getPhone());
            stmt.setString(6, staff.getEmail());
            stmt.setDate(7, staff.getJoined() != null ? Date.valueOf(staff.getJoined()) : null);
            stmt.setDouble(8, staff.getSalary());
            stmt.setString(9, staff.getRole());
            stmt.executeUpdate();
        }
    }

    public void softDeleteStaff(int hotelId, int staffId) throws SQLException {
        String query = "UPDATE Staff SET is_active = FALSE WHERE hotel_id=? AND staff_id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            stmt.setInt(2, staffId);
            int rows = stmt.executeUpdate();
            if (rows == 0)
                throw new SQLException("Staff not found.");
        }
    }

    public List<Staff> getDeletedStaff(int hotelId) {
        List<Staff> staff = new ArrayList<>();
        String query = "SELECT * FROM Staff WHERE hotel_id = ? AND is_active = FALSE";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                staff.add(new Staff(
                        rs.getInt("hotel_id"),
                        rs.getInt("staff_id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getDate("join_date") != null ? rs.getDate("join_date").toLocalDate() : null,
                        rs.getDouble("salary"),
                        rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staff;
    }

    public void updateStaff(Staff staff, int staffId) throws SQLException {
        String query = "UPDATE staff SET staff_id = ?, name=?, gender=?, phone=?, email=?, join_date=?, salary = ?, role=? WHERE hotel_id=? AND staff_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, staff.getStaffId());
            stmt.setString(2, staff.getName());
            stmt.setString(3, staff.getGender());
            stmt.setString(4, staff.getPhone());
            stmt.setString(5, staff.getEmail());
            stmt.setDate(6, staff.getJoined() != null ? Date.valueOf(staff.getJoined()) : null);
            stmt.setDouble(7, staff.getSalary());
            stmt.setString(8, staff.getRole());
            stmt.setInt(9, staff.getHotelId());
            stmt.setInt(10, staffId);
            stmt.executeUpdate();
        }
    }

}