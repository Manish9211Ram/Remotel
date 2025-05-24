package com.hostel.model;

import java.util.Date;

public class RoomRequest {
    private int requestId;
    private int userId;
    private String preferredRoomType;
    private String status; // pending, approved, rejected
    private Date requestDate;
    private String remarks;
    private Date preferredCheckoutDate;

    public RoomRequest() {}

    public RoomRequest(int requestId, int userId, String preferredRoomType, String status, Date requestDate, String remarks) {
        this.requestId = requestId;
        this.userId = userId;
        this.preferredRoomType = preferredRoomType;
        this.status = status;
        this.requestDate = requestDate;
        this.remarks = remarks;
    }

    public RoomRequest(int requestId, int userId, String preferredRoomType, String status, Date requestDate, String remarks, Date preferredCheckoutDate) {
        this(requestId, userId, preferredRoomType, status, requestDate, remarks);
        this.preferredCheckoutDate = preferredCheckoutDate;
    }

    // Getters and Setters
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getPreferredRoomType() { return preferredRoomType; }
    public void setPreferredRoomType(String preferredRoomType) { this.preferredRoomType = preferredRoomType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getRequestDate() { return requestDate; }
    public void setRequestDate(Date requestDate) { this.requestDate = requestDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Date getPreferredCheckoutDate() { return preferredCheckoutDate; }
    public void setPreferredCheckoutDate(Date preferredCheckoutDate) { this.preferredCheckoutDate = preferredCheckoutDate; }
}