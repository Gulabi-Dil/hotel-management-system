package dao;

import java.sql.*;
import java.time.LocalDate;

import models.BillDetails;
import models.Payment;
import java.util.List;
import java.util.ArrayList;
import utils.DBConnection;

public class paymentDao {

    public List<Payment> getPaymentsByHotel(int hotelId) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT p.booking_id, p.extra_services_cost, p.payment_date, p.mode, " +
                "p.status AS payment_status, p.rating, b.status AS booking_status, " +
                "ROUND((r.price_per_day * (b.check_out - b.check_in) * 1.2 + COALESCE(p.extra_services_cost, 0)), 2) AS bill " +
                "FROM Payment p " +
                "JOIN Booking b ON p.booking_id = b.booking_id " +
                "JOIN Room r ON b.hotel_id = r.hotel_id AND b.room_number = r.room_number " +
                "WHERE b.hotel_id = ? " +
                "ORDER BY p.booking_id";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate payDate = rs.getDate("payment_date") != null
                        ? rs.getDate("payment_date").toLocalDate()
                        : null;
                payments.add(new Payment(
                        rs.getInt("booking_id"),
                        rs.getDouble("extra_services_cost"),
                        payDate,
                        rs.getString("mode"),
                        rs.getString("payment_status"),
                        rs.getDouble("rating"),
                        rs.getDouble("bill"),
                        rs.getString("booking_status")
                    ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public List<Integer> getBookingIdsByHotel(int hotelId) {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT booking_id FROM Booking WHERE hotel_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                ids.add(rs.getInt("booking_id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public void updatePayment(int bookingId, double extraService, String mode, String status, double rating)
            throws SQLException {
        String query = "UPDATE Payment SET extra_services_cost = ?, mode = ?, status = ?, rating = ? WHERE booking_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDouble(1, extraService);
                stmt.setString(2, mode);
                stmt.setString(3, status);
                stmt.setDouble(4, rating);
                stmt.setInt(5, bookingId);
                stmt.executeUpdate();
            }

            conn.commit();

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

    public BillDetails getBillDetails(int bookingId) {
        String query = "SELECT b.booking_id, c.name AS customer_name, c.phone_number, " +
                "r.room_type, b.room_number, r.price_per_day, " +
                "b.check_in, b.check_out, " +
                "(b.check_out - b.check_in) AS days_stayed, " +
                "COALESCE(p.extra_services_cost, 0) AS extra_services, " +
                "ROUND((r.price_per_day * (b.check_out - b.check_in) * 1.2 + COALESCE(p.extra_services_cost, 0)), 2) AS total_bill "
                +
                "FROM Booking b " +
                "JOIN Customer c ON b.aadhar_num = c.aadhar_num " +
                "JOIN Room r ON b.hotel_id = r.hotel_id AND b.room_number = r.room_number " +
                "JOIN Payment p ON b.booking_id = p.booking_id " +
                "WHERE b.booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new BillDetails(
                        rs.getInt("booking_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone_number"),
                        rs.getString("room_type"),
                        rs.getInt("room_number"),
                        rs.getDouble("price_per_day"),
                        rs.getDate("check_in").toLocalDate(),
                        rs.getDate("check_out").toLocalDate(),
                        rs.getInt("days_stayed"),
                        rs.getDouble("extra_services"),
                        rs.getDouble("total_bill"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}