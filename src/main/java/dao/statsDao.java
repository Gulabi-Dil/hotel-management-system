package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;

public class statsDao {
    public List<String> getRevenueMonths() {
        List<String> months = new ArrayList<>();
         String query =
        "SELECT TO_CHAR(date_trunc('month', b.check_out), 'Mon YYYY') AS month " +
        "FROM Payment p " +
        "JOIN Booking b ON p.booking_id = b.booking_id " +
        "WHERE p.status = 'completed' " +
        "AND b.check_out >= CURRENT_DATE - INTERVAL '6 months' " +
        "GROUP BY date_trunc('month', b.check_out) " +
        "ORDER BY date_trunc('month', b.check_out)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) months.add(rs.getString("month"));
        } catch (SQLException e) { e.printStackTrace(); }
        return months;
    }

    public List<Double> getRevenueAmounts() {
        List<Double> amounts = new ArrayList<>();
        String query =
        "SELECT SUM(r.price_per_day * (b.check_out - b.check_in) + COALESCE(p.extra_services_cost, 0)) AS revenue " +
        "FROM Payment p " +
        "JOIN Booking b ON p.booking_id = b.booking_id " +
        "JOIN Room r ON b.hotel_id = r.hotel_id AND b.room_number = r.room_number " +
        "WHERE p.status = 'completed' " +
        "AND b.check_out >= CURRENT_DATE - INTERVAL '6 months' " +
        "GROUP BY date_trunc('month', b.check_out) " +
        "ORDER BY date_trunc('month', b.check_out)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) amounts.add(rs.getDouble("revenue"));
        } catch (SQLException e) { e.printStackTrace(); }
        return amounts;
    }

    // Bookings per hotel
    public List<String> getHotelNames() {
        List<String> names = new ArrayList<>();
        String query =
            "SELECT h.hotel_name, COUNT(b.booking_id) AS total " +
            "FROM Hotel h " +
            "LEFT JOIN Booking b ON h.hotel_id = b.hotel_id " +
            "WHERE h.is_active = TRUE " +
            "GROUP BY h.hotel_name " +
            "ORDER BY total DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) names.add(rs.getString("hotel_name"));
        } catch (SQLException e) { e.printStackTrace(); }
        return names;
    }

    public List<Integer> getHotelBookingCounts() {
        List<Integer> counts = new ArrayList<>();
        String query =
            "SELECT h.hotel_name, COUNT(b.booking_id) AS total " +
            "FROM Hotel h " +
            "LEFT JOIN Booking b ON h.hotel_id = b.hotel_id " +
            "WHERE h.is_active = TRUE " +
            "GROUP BY h.hotel_name " +
            "ORDER BY total DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) counts.add(rs.getInt("total"));
        } catch (SQLException e) { e.printStackTrace(); }
        return counts;
    }
}