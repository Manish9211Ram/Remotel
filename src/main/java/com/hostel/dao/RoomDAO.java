package com.hostel.dao;

import com.hostel.model.Room;
import com.hostel.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RoomDAO {
    
    public boolean addRoom(Room room) {
        String query = "INSERT INTO rooms (room_number, room_type, is_occupied, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType());
            pstmt.setBoolean(3, room.isOccupied());
            pstmt.setString(4, room.getStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms WHERE status = 'available' AND is_occupied = false";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public boolean updateRoomStatus(int roomId, String status, boolean isOccupied, Integer occupantId) {
        String query = "UPDATE rooms SET status = ?, is_occupied = ?, occupant_id = ? WHERE room_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setBoolean(2, isOccupied);
            if (occupantId != null) {
                pstmt.setInt(3, occupantId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setInt(4, roomId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean allocateRoom(int roomId, int userId, Date expectedCheckoutDate) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // Update room status
            String updateRoomQuery = "UPDATE rooms SET is_occupied = ?, occupant_id = ?, status = ?, allocation_date = CURRENT_TIMESTAMP, expected_checkout_date = ? WHERE room_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRoomQuery)) {
                pstmt.setBoolean(1, true);
                pstmt.setInt(2, userId);
                pstmt.setString(3, "occupied");
                pstmt.setTimestamp(4, new Timestamp(expectedCheckoutDate.getTime()));
                pstmt.setInt(5, roomId);
                pstmt.executeUpdate();
            }

            // Add entry to allocation history
            String insertHistoryQuery = "INSERT INTO room_allocation_history (room_id, user_id, allocation_date, status) VALUES (?, ?, CURRENT_TIMESTAMP, 'active')";
            try (PreparedStatement pstmt = conn.prepareStatement(insertHistoryQuery)) {
                pstmt.setInt(1, roomId);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
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
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean deallocateRoom(int roomId, String remarks) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // Update room status
            String updateRoomQuery = "UPDATE rooms SET is_occupied = FALSE, occupant_id = NULL, status = 'available', allocation_date = NULL, expected_checkout_date = NULL WHERE room_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRoomQuery)) {
                pstmt.setInt(1, roomId);
                pstmt.executeUpdate();
            }

            // Update allocation history
            String updateHistoryQuery = "UPDATE room_allocation_history SET checkout_date = CURRENT_TIMESTAMP, status = 'completed', remarks = ? WHERE room_id = ? AND status = 'active'";
            try (PreparedStatement pstmt = conn.prepareStatement(updateHistoryQuery)) {
                pstmt.setString(1, remarks);
                pstmt.setInt(2, roomId);
                pstmt.executeUpdate();
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
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Room> getRoomsWithAllocationInfo() {
        List<Room> rooms = new ArrayList<>();
        String query = """
            SELECT r.*, u.name as occupant_name 
            FROM rooms r 
            LEFT JOIN users u ON r.occupant_id = u.id
            ORDER BY r.room_number
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Room room = extractRoomFromResultSet(rs);
                // Add occupant name if available
                if (rs.getString("occupant_name") != null) {
                    room.setOccupantName(rs.getString("occupant_name"));
                }
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public List<Map<String, Object>> getAllocationHistory(int roomId) {
        List<Map<String, Object>> history = new ArrayList<>();
        String query = """
            SELECT rh.*, r.room_number, u.name as user_name
            FROM room_allocation_history rh
            JOIN rooms r ON rh.room_id = r.room_id
            JOIN users u ON rh.user_id = u.id
            WHERE rh.room_id = ?
            ORDER BY rh.allocation_date DESC
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("allocation_id", rs.getInt("allocation_id"));
                    record.put("room_number", rs.getString("room_number"));
                    record.put("user_name", rs.getString("user_name"));
                    record.put("allocation_date", rs.getTimestamp("allocation_date"));
                    record.put("checkout_date", rs.getTimestamp("checkout_date"));
                    record.put("status", rs.getString("status"));
                    record.put("remarks", rs.getString("remarks"));
                    history.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    private Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getString("room_type"),
            rs.getBoolean("is_occupied"),
            rs.getInt("occupant_id"),
            rs.getString("status")
        );
    }
}