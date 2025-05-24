package com.hostel.dao;

import com.hostel.model.RoomRequest;
import com.hostel.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomRequestDAO {
    
    public boolean createRequest(RoomRequest request) {
        String query = "INSERT INTO room_requests (user_id, preferred_room_type, status, remarks, preferred_checkout_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, request.getUserId());
            pstmt.setString(2, request.getPreferredRoomType());
            pstmt.setString(3, "pending");
            pstmt.setString(4, request.getRemarks());
            if (request.getPreferredCheckoutDate() != null) {
                pstmt.setTimestamp(5, new Timestamp(request.getPreferredCheckoutDate().getTime()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RoomRequest> getAllRequests() {
        List<RoomRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM room_requests ORDER BY request_date DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public List<RoomRequest> getRequestsByUser(int userId) {
        List<RoomRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM room_requests WHERE user_id = ? ORDER BY request_date DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(extractRequestFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public boolean updateRequestStatus(int requestId, String status, String remarks) {
        String query = "UPDATE room_requests SET status = ?, remarks = ? WHERE request_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, remarks);
            pstmt.setInt(3, requestId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RoomRequest extractRequestFromResultSet(ResultSet rs) throws SQLException {
        RoomRequest request = new RoomRequest(
            rs.getInt("request_id"),
            rs.getInt("user_id"),
            rs.getString("preferred_room_type"),
            rs.getString("status"),
            rs.getTimestamp("request_date"),
            rs.getString("remarks")
        );
        Timestamp checkoutDate = rs.getTimestamp("preferred_checkout_date");
        if (checkoutDate != null) {
            request.setPreferredCheckoutDate(checkoutDate);
        }
        return request;
    }
}
