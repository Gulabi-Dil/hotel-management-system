package dao;

import java.sql.*;
import java.time.LocalDate;

import models.Booking;
import java.util.List;
import java.util.ArrayList;
import utils.DBConnection;

public class bookingDao {
    public List<Booking> getBookingsByHotel(int hotelId) {
        if (hotelId <= 0)
            throw new IllegalArgumentException("Invalid hotel ID.");
        List<Booking> hotelBookings = new ArrayList<>();

        String query = "SELECT b.*,r.room_type FROM booking b JOIN room r ON b.hotel_id = r.hotel_id AND b.room_number = r.room_number WHERE b.hotel_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                hotelBookings.add(
                        new Booking(
                                rs.getInt("booking_id"),
                                rs.getString("aadhar_num"),
                                rs.getInt("hotel_id"),
                                rs.getString("room_type"),
                                rs.getInt("room_number"),
                                rs.getDate("check_in") != null ? rs.getDate("check_in").toLocalDate() : null,
                                rs.getString("status"),
                                rs.getDate("check_out") != null ? rs.getDate("check_out").toLocalDate() : null));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hotelBookings;
    }

    public List<String> getAvailableRoomTypesByHotel(int hotelId, LocalDate checkIn, LocalDate checkOut) {
        List<String> typesAvailable = new ArrayList<>();
        String query = "SELECT DISTINCT r.room_type FROM Room r " +
                "WHERE r.hotel_id = ? AND r.status != 'decommissioned' " +
                "AND r.room_number NOT IN (" +
                "    SELECT b.room_number FROM Booking b " +
                "    WHERE b.hotel_id = ? " +
                "    AND b.status <> 'checked_out' " +
                "    AND b.check_in < ? AND b.check_out > ?" +
                ")";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            stmt.setInt(2, hotelId);
            stmt.setDate(3, Date.valueOf(checkOut));
            stmt.setDate(4, Date.valueOf(checkIn));
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                typesAvailable.add(rs.getString("room_type"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return typesAvailable;
    }

    public List<Integer> getAvailableRoomNumbers(int hotelId, String type, LocalDate checkIn, LocalDate checkOut) {
        List<Integer> rooms = new ArrayList<>();
        if (type == null)
            return rooms;

        // Room is available if:
        // 1. It exists and is not decommissioned
        // 2. It has no overlapping active bookings in the requested date range
        String query = "SELECT r.room_number FROM Room r " +
                "WHERE r.hotel_id = ? AND r.room_type = ? AND r.status != 'decommissioned' " +
                "AND r.room_number NOT IN (" +
                "    SELECT b.room_number FROM Booking b " +
                "    WHERE b.hotel_id = ? " +
                "    AND b.status <> 'checked_out' " +
                "    AND b.check_in < ? AND b.check_out > ?" +
                ")";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            stmt.setString(2, type);
            stmt.setInt(3, hotelId);
            stmt.setDate(4, Date.valueOf(checkOut)); // existing check_in < new check_out
            stmt.setDate(5, Date.valueOf(checkIn)); // existing check_out > new check_in
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                rooms.add(rs.getInt("room_number"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public List<String> getAllAadharNumbers() {
        List<String> registeredAadhars = new ArrayList<>();
        String query = "SELECT aadhar_num FROM customer";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                registeredAadhars.add(rs.getString("aadhar_num"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registeredAadhars;

    }

    public void addBooking(Booking booking) throws SQLException {
        String query = "INSERT INTO booking(aadhar_num,hotel_id,room_number,check_in,status,check_out) VALUES(?,?,?,?,?,?)";
        if (booking == null)
            throw new IllegalArgumentException("Booking cannot be null.");
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            stmt.setString(1, booking.getAadhar());
            stmt.setInt(2, booking.getHotelId());
            stmt.setInt(3, booking.getRoomNum());
            stmt.setDate(4, booking.getCheckIn() != null ? Date.valueOf(booking.getCheckIn()) : null);
            stmt.setString(5, booking.getStatus());
            stmt.setDate(6, booking.getCheckOut() != null ? Date.valueOf(booking.getCheckOut()) : null);

            stmt.executeUpdate();
        } catch (SQLException e) {
            // Identify constraint violation
            switch (e.getSQLState()) {
                case "23505": // unique violation
                    throw new SQLException("Booking ID already exists.", e);
                case "23503": // foreign key violation
                    if (e.getMessage().contains("booking_aadhar_num_fkey"))
                        throw new SQLException("Customer with this Aadhar number does not exist.", e);
                    else if (e.getMessage().contains("booking_hotel_id_room_number_fkey")) {
                        throw new SQLException("Room does not exist in this hotel.", e);
                    }
                    break;
                case "23514": // check violation
                    if (e.getMessage().contains("booking_check"))
                        throw new SQLException("Check-out date must be after check-in date.", e);
                    else if (e.getMessage().contains("booking_status_check")) {
                        throw new SQLException("Status must be one of confirmed, checked_in, checked_out, cancelled.",
                                e);
                    }
                    break;
                case "23502": // not null
                    throw new SQLException("All fields are required.", e);
                default:
                    throw e; // unknown exceptions
            }
        }
    }

    public void updateBooking(Booking booking) throws SQLException {
        String query = "";
        if (booking.getStatus() == "cancelled") {
            query = "DELETE FROM booking WHERE booking_id = ?";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query.toString())) {
                stmt.setInt(1, booking.getBookingId());
                stmt.executeUpdate();
            }
        } else {
            query = "UPDATE booking SET aadhar_num = ?, hotel_id = ?, room_number = ?, check_in = ?, status = ?, check_out = ? WHERE booking_id = ?";
            try (Connection conn = DBConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(query.toString())) {
                stmt.setString(1, booking.getAadhar());
                stmt.setInt(2, booking.getHotelId());
                stmt.setInt(3, booking.getRoomNum());
                stmt.setDate(4, booking.getCheckIn() != null ? Date.valueOf(booking.getCheckIn()) : null);
                stmt.setString(5, booking.getStatus());
                stmt.setDate(6, booking.getCheckOut() != null ? Date.valueOf(booking.getCheckOut()) : null);
                stmt.setInt(7, booking.getBookingId());

                stmt.executeUpdate();
            }
        }
    }
}
