package dao;

import models.Hotel;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import java.sql.*;

public class hotelDao {

    private int getCount(String query) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalHotels() {
        return getCount("SELECT COUNT(*) FROM Hotel WHERE is_active = TRUE");
    }

    public int getTotalRooms() {
        return getCount("SELECT COUNT(*) FROM Room");
    }

    public int getTotalBookings() {
        return getCount("SELECT COUNT(*) FROM Booking");
    }

    public int getTotalStaff() {
        return getCount("SELECT COUNT(*) FROM Staff");
    }

    private Hotel mapHotel(ResultSet rs) throws SQLException {
        return new Hotel(
                rs.getInt("hotel_id"),
                rs.getString("hotel_name"),
                rs.getString("streetName"),
                rs.getString("landmark"),
                rs.getString("phones"),
                rs.getString("rating"),
                rs.getBoolean("is_active"));
    }

    public List<Hotel> getAllActiveHotels() {
        List<Hotel> hotels = new ArrayList<>();
        String query = "SELECT h.hotel_id, h.hotel_name, h.streetName, h.landmark, " +
                "STRING_AGG(DISTINCT pn.phone_number, ',') AS phones, " +
                "ROUND(AVG(p.rating), 1) AS rating, h.is_active " +
                "FROM Hotel h " +
                "LEFT JOIN HotelPhone pn ON h.hotel_id = pn.hotel_id " +
                "LEFT JOIN Booking b ON h.hotel_id = b.hotel_id " +
                "LEFT JOIN Payment p ON b.booking_id = p.booking_id " +
                "WHERE h.is_active = TRUE " + 
                "GROUP BY h.hotel_id " +
                "ORDER BY h.hotel_id";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                hotels.add(mapHotel(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hotels;
    }

    public Hotel getHotelById(int hotelId) {
        String query = "SELECT h.hotel_id, h.hotel_name, h.streetName, h.landmark, " +
                "STRING_AGG(DISTINCT n.phone_number, ', ') AS phones, " +
                "ROUND(AVG(p.rating), 1) AS rating, h.is_active" +
                "FROM Hotel h " +
                "LEFT JOIN HotelPhone pn ON h.hotel_id = pn.hotel_id " +
                "LEFT JOIN Booking b ON h.hotel_id = b.hotel_id " +
                "LEFT JOIN Payment p ON b.booking_id = p.booking_id " +
                "WHERE h.hotel_id = ? " +
                "GROUP BY h.hotel_id";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return mapHotel(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String addHotel(String name, String street, String landmark, List<String> phones) {
        String checkQuery = "SELECT hotel_id, is_active FROM Hotel WHERE hotel_name = ? AND streetName = ? AND landmark = ?";
        String reactivateQuery = "UPDATE Hotel SET is_active = TRUE, hotel_name = ?, streetName = ?, landmark = ? WHERE hotel_id = ?";
        String insertHotelQuery = "INSERT INTO Hotel(hotel_name, streetName, landmark) VALUES(?,?,?) RETURNING hotel_id";
        String insertPhoneQuery = "INSERT INTO HotelPhone(hotel_id, phone_number) VALUES(?,?)";
        String deletePhoneQuery = "DELETE FROM HotelPhone WHERE hotel_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, name);
                checkStmt.setString(2, street);
                checkStmt.setString(3, landmark);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    boolean isActive = rs.getBoolean("is_active");
                    int existingId = rs.getInt("hotel_id");

                    if (!isActive) {
                        // Reactivate hotel
                        try (PreparedStatement reactivateStmt = conn.prepareStatement(reactivateQuery)) {
                            reactivateStmt.setString(1, name);
                            reactivateStmt.setString(2, street);
                            reactivateStmt.setString(3, landmark);
                            reactivateStmt.setInt(4, existingId);
                            reactivateStmt.executeUpdate();
                        }
                        // Replace phones
                        try (PreparedStatement delStmt = conn.prepareStatement(deletePhoneQuery)) {
                            delStmt.setInt(1, existingId);
                            delStmt.executeUpdate();
                        }
                        for (String phone : phones) {
                            try (PreparedStatement pStmt = conn.prepareStatement(insertPhoneQuery)) {
                                pStmt.setInt(1, existingId);
                                pStmt.setString(2, phone.trim());
                                pStmt.executeUpdate();
                            }
                        }
                        conn.commit();
                        return "Hotel reactivated successfully!";
                    } else {
                        conn.rollback();
                        return "Hotel already exists and is active.";
                    }
                } else {
                    // Fresh insert
                    int newHotelId;
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertHotelQuery)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, street);
                        insertStmt.setString(3, landmark);
                        ResultSet insertRs = insertStmt.executeQuery();
                        insertRs.next();
                        newHotelId = insertRs.getInt("hotel_id");
                    }
                    for (String phone : phones) {
                        try (PreparedStatement pStmt = conn.prepareStatement(insertPhoneQuery)) {
                            pStmt.setInt(1, newHotelId);
                            pStmt.setString(2, phone.trim());
                            pStmt.executeUpdate();
                        }
                    }
                    conn.commit();
                    return "success";
                }
            } catch (SQLException e) {
                conn.rollback();
                if (e.getSQLState().equals("23505")) {
                    return "Phone number already exists.";
                }
                e.printStackTrace();
                return "Database error. Please try again.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error connecting to database.";
        }
    }

    public boolean updateHotel(int hotelId, String name, String street, String landmark, List<String> phones) {
        String updateHotelQuery = "UPDATE Hotel SET hotel_name = ?, streetName = ?, landmark = ? WHERE hotel_id = ?";
        String selectPhonesQuery = "SELECT phone_number FROM HotelPhone WHERE hotel_id = ?";
        String insertPhoneQuery = "INSERT INTO HotelPhone(hotel_id, phone_number) VALUES(?, ?)";
        String deletePhoneQuery = "DELETE FROM HotelPhone WHERE hotel_id = ? AND phone_number = ?";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement updateStmt = conn.prepareStatement(updateHotelQuery)) {
                updateStmt.setString(1, name);
                updateStmt.setString(2, street);
                updateStmt.setString(3, landmark);
                updateStmt.setInt(4, hotelId);
                updateStmt.executeUpdate();
            }
            List<String> existingPhones = new ArrayList<>();
            try (PreparedStatement selectStmt = conn.prepareStatement(selectPhonesQuery)) {
                selectStmt.setInt(1, hotelId);
                ResultSet rs = selectStmt.executeQuery();
                while (rs.next()) {
                    existingPhones.add(rs.getString("phone_number"));
                }
            }
            try (PreparedStatement insertStmt = conn.prepareStatement(insertPhoneQuery)) {
                for (String phone : phones) {
                    if (!existingPhones.contains(phone.trim())) {
                        insertStmt.setInt(1, hotelId);
                        insertStmt.setString(2, phone.trim());
                        insertStmt.executeUpdate();
                    }
                }
            }
            try (PreparedStatement deleteStmt = conn.prepareStatement(deletePhoneQuery)) {
                for (String oldPhone : existingPhones) {
                    if (!phones.contains(oldPhone.trim())) {
                        deleteStmt.setInt(1, hotelId);
                        deleteStmt.setString(2, oldPhone);
                        deleteStmt.executeUpdate();
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean softDeleteHotel(int hotelId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn
                    .prepareStatement("UPDATE Hotel SET is_active = FALSE WHERE hotel_id = ?")) {
                stmt.setInt(1, hotelId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
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

    public List<Hotel> getDeletedHotels() {
        List<Hotel> hotels = new ArrayList<>();
        String query = "SELECT h.hotel_id, h.hotel_name, h.streetName, h.landmark, " +
                "STRING_AGG(DISTINCT pn.phone_number, ',') AS phones, " +
                "ROUND(AVG(p.rating), 1) AS rating, h.is_active " +
                "FROM Hotel h " +
                "LEFT JOIN HotelPhone pn ON h.hotel_id = pn.hotel_id " +
                "LEFT JOIN Booking b ON h.hotel_id = b.hotel_id " +
                "LEFT JOIN Payment p ON b.booking_id = p.booking_id " +
                "WHERE h.is_active = FALSE " +
                "GROUP BY h.hotel_id " +
                "ORDER BY h.hotel_id";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                hotels.add(mapHotel(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hotels;
    }

}