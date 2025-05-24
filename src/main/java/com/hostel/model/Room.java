package com.hostel.model;

import java.util.Date;

public class Room {
    private int roomId;
    private String roomNumber;
    private String roomType;
    private boolean isOccupied;
    private int occupantId;
    private String status; // available, allocated, maintenance
    private Date allocationDate;
    private Date expectedCheckoutDate;
    private String occupantName; // To display in UI

    public Room() {}

    public Room(int roomId, String roomNumber, String roomType, boolean isOccupied, int occupantId, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.isOccupied = isOccupied;
        this.occupantId = occupantId;
        this.status = status;
    }

    // Getters and Setters
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }

    public int getOccupantId() { return occupantId; }
    public void setOccupantId(int occupantId) { this.occupantId = occupantId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getAllocationDate() { return allocationDate; }
    public void setAllocationDate(Date allocationDate) { this.allocationDate = allocationDate; }

    public Date getExpectedCheckoutDate() { return expectedCheckoutDate; }
    public void setExpectedCheckoutDate(Date expectedCheckoutDate) { this.expectedCheckoutDate = expectedCheckoutDate; }

    public String getOccupantName() { return occupantName; }
    public void setOccupantName(String occupantName) { this.occupantName = occupantName; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Room ").append(roomNumber);
        sb.append(" (").append(roomType).append(")");
        if (isOccupied && occupantName != null) {
            sb.append(" - Occupied by: ").append(occupantName);
        } else {
            sb.append(" - ").append(status);
        }
        return sb.toString();
    }
}