package dao;

import models.*;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import java.sql.*;

public class roomDao {
    public List<RoomType> getRoomTypesByHotel(int hotelId) {
        List<RoomType> roomsList = new ArrayList<>();
        String query = "SELECT room_type, SUM(CASE WHEN status = 'available' THEN 1 ELSE 0 END) AS available, COUNT(*) AS total "
                +
                "FROM Room WHERE hotel_id = ? AND status <> 'decommissioned' GROUP BY room_type";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roomsList.add(new RoomType(
                        rs.getString("room_type"),
                        rs.getInt("available"),
                        rs.getInt("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomsList;
    }

    public List<Room> getRoomsByHotel(int hotelId) {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM Room WHERE hotel_id = ? AND status <> 'decommissioned'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("hotel_id"),
                        rs.getInt("room_number"),
                        rs.getString("room_type"),
                        rs.getDouble("price_per_day"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public void deleteRoom(int hotelId, int roomNum) throws SQLException {
        String statusQuery = "SELECT status FROM Room WHERE hotel_id = ? AND room_number = ?";
        String updateQuery = "UPDATE Room SET status = 'decommissioned' WHERE hotel_id = ? AND room_number = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(statusQuery)) {
                stmt.setInt(1, hotelId);
                stmt.setInt(2, roomNum);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String status = rs.getString("status");
                    if (status.equals("booked") || status.equals("occupied")) {
                        throw new SQLException("Cannot delete room: Room is currently booked or occupied.");
                    }
                } else {
                    throw new SQLException("Room does not exist.");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, hotelId);
                stmt.setInt(2, roomNum);
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

    public List<Room> getDeletedRooms(int hotelId) {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM Room WHERE hotel_id = ? AND status = 'decommissioned'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("hotel_id"),
                        rs.getInt("room_number"),
                        rs.getString("room_type"),
                        rs.getDouble("price_per_day"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public void addRoom(Room room) throws SQLException {
        String checkQuery = "SELECT status FROM Room WHERE hotel_id = ? AND room_number = ?";
        String insertQuery = "INSERT INTO Room(hotel_id, room_number, room_type, price_per_day, status) VALUES (?, ?, ?, ?, ?)";
        String updateQuery = "UPDATE Room SET room_type = ?, price_per_day = ?, status = ? WHERE hotel_id = ? AND room_number = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, room.getHotelId());
                checkStmt.setInt(2, room.getRoomNum());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    if (status.equals("decommissioned")) {
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, room.getType());
                            updateStmt.setDouble(2, room.getPricePerDay());
                            updateStmt.setString(3, room.getStatus());
                            updateStmt.setInt(4, room.getHotelId());
                            updateStmt.setInt(5, room.getRoomNum());
                            updateStmt.executeUpdate();
                        }
                    } else {
                        throw new SQLException("Room already exists and is active.", "23505");
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, room.getHotelId());
                        insertStmt.setInt(2, room.getRoomNum());
                        insertStmt.setString(3, room.getType());
                        insertStmt.setDouble(4, room.getPricePerDay());
                        insertStmt.setString(5, room.getStatus());
                        insertStmt.executeUpdate();
                    }
                }
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

    public String updateRoom(Room room, int oldRoomNum) throws SQLException {
        String checkQuery = "SELECT status, price_per_day FROM Room WHERE hotel_id = ? AND room_number = ?";
        String updateQuery = "UPDATE room SET room_number = ?, room_type = ?, price_per_day = ?, status = ? WHERE hotel_id = ? AND room_number = ?";

        try (Connection conn = DBConnection.getConnection();PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, room.getHotelId());  checkStmt.setInt(2, oldRoomNum);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                String currentStatus = rs.getString("status");
                double currentPrice = rs.getDouble("price_per_day");
                if ((currentStatus.equals("booked") || currentStatus.equals("occupied")) && room.getPricePerDay() != currentPrice) {
                    return "Cannot change price: Room is currently " + currentStatus + ".";
                }
            }
        }

        try (Connection conn = DBConnection.getConnection();PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, room.getRoomNum()); stmt.setString(2, room.getType());
            stmt.setDouble(3, room.getPricePerDay()); stmt.setString(4, room.getStatus());
            stmt.setInt(5, room.getHotelId()); stmt.setInt(6, oldRoomNum);
            stmt.executeUpdate();
        }
        return null;
    }
}