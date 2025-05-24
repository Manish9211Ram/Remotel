package com.hostel.ui;

import com.hostel.dao.RoomDAO;
import com.hostel.dao.RoomRequestDAO;
import com.hostel.model.Room;
import com.hostel.model.RoomRequest;
import com.hostel.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.sql.Timestamp;

public class AdminDashboard extends JFrame {
    private final User admin;
    private final RoomDAO roomDAO;
    private final RoomRequestDAO requestDAO;
    private JTable roomsTable;
    private JTable requestsTable;
    private DefaultTableModel roomsModel;
    private DefaultTableModel requestsModel;
    private JTextField searchField;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public AdminDashboard(User admin) {
        this.admin = admin;
        this.roomDAO = new RoomDAO();
        this.requestDAO = new RoomRequestDAO();

        setTitle("Admin Dashboard - Remotel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Top panel with welcome message and search
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel with tables
        JSplitPane splitPane = createSplitPane();
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Bottom panel with actions
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Load initial data
        refreshTables();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome, " + admin.getName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(welcomeLabel, BorderLayout.WEST);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(37, 99, 235));
        searchButton.setForeground(Color.BLACK);

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.EAST);

        // Add search functionality
        searchButton.addActionListener(e -> filterRooms());
        searchField.addActionListener(e -> filterRooms());

        return panel;
    }

    private JSplitPane createSplitPane() {
        // Rooms table
        roomsModel = new DefaultTableModel(
            new Object[]{"Room ID", "Room Number", "Type", "Status", "Occupant", "Allocation Date", "Expected Checkout"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomsTable = new JTable(roomsModel);
        JScrollPane roomsScrollPane = new JScrollPane(roomsTable);
        roomsScrollPane.setBorder(BorderFactory.createTitledBorder("Rooms"));

        // Requests table - Added Preferred Checkout column
        requestsModel = new DefaultTableModel(
            new Object[]{"Request ID", "User ID", "Room Type", "Status", "Request Date", "Preferred Checkout", "Remarks"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        requestsTable = new JTable(requestsModel);
        JScrollPane requestsScrollPane = new JScrollPane(requestsTable);
        requestsScrollPane.setBorder(BorderFactory.createTitledBorder("Room Requests"));

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, roomsScrollPane, requestsScrollPane);
        splitPane.setResizeWeight(0.5);

        // Add double-click listeners
        roomsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showRoomHistory();
                }
            }
        });

        requestsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleRequest();
                }
            }
        });

        return splitPane;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);

        JButton addRoomButton = new JButton("Add Room");
        addRoomButton.setBackground(new Color(22, 163, 74));
        addRoomButton.setForeground(Color.BLACK);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(220, 38, 38));
        logoutButton.setForeground(Color.BLACK);

        panel.add(addRoomButton);
        panel.add(logoutButton);

        addRoomButton.addActionListener(e -> addNewRoom());
        logoutButton.addActionListener(e -> logout());

        return panel;
    }

    private void refreshTables() {
        // Clear existing data
        roomsModel.setRowCount(0);
        requestsModel.setRowCount(0);

        // Load rooms with allocation info
        List<Room> rooms = roomDAO.getRoomsWithAllocationInfo();
        for (Room room : rooms) {
            roomsModel.addRow(new Object[]{
                room.getRoomId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getStatus(),
                room.getOccupantName() != null ? room.getOccupantName() : "-",
                room.getAllocationDate() != null ? dateFormat.format(room.getAllocationDate()) : "-",
                room.getExpectedCheckoutDate() != null ? dateFormat.format(room.getExpectedCheckoutDate()) : "-"
            });
        }

        // Load requests
        List<RoomRequest> requests = requestDAO.getAllRequests();
        for (RoomRequest request : requests) {
            String checkoutDate = request.getPreferredCheckoutDate() != null ? 
                dateFormat.format(request.getPreferredCheckoutDate()) : "-";
            requestsModel.addRow(new Object[]{
                request.getRequestId(),
                request.getUserId(),
                request.getPreferredRoomType(),
                request.getStatus(),
                dateFormat.format(request.getRequestDate()),
                checkoutDate,
                request.getRemarks()
            });
        }
    }

    private void filterRooms() {
        String searchText = searchField.getText().toLowerCase();
        roomsModel.setRowCount(0);

        List<Room> rooms = roomDAO.getRoomsWithAllocationInfo();
        for (Room room : rooms) {
            if (room.getRoomNumber().toLowerCase().contains(searchText) ||
                room.getRoomType().toLowerCase().contains(searchText) ||
                room.getStatus().toLowerCase().contains(searchText)) {

                roomsModel.addRow(new Object[]{
                    room.getRoomId(),
                    room.getRoomNumber(),
                    room.getRoomType(),
                    room.getStatus(),
                    room.getOccupantName() != null ? room.getOccupantName() : "-",
                    room.getAllocationDate() != null ? dateFormat.format(room.getAllocationDate()) : "-",
                    room.getExpectedCheckoutDate() != null ? dateFormat.format(room.getExpectedCheckoutDate()) : "-"
                });
            }
        }
    }

    private void addNewRoom() {
        JTextField roomNumberField = new JTextField();
        JComboBox<String> roomTypeCombo = new JComboBox<>(new String[]{"Single", "Double", "Triple"});

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Room Number:"));
        panel.add(roomNumberField);
        panel.add(new JLabel("Room Type:"));
        panel.add(roomTypeCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Room",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String roomNumber = roomNumberField.getText();
            String roomType = (String) roomTypeCombo.getSelectedItem();

            if (!roomNumber.isEmpty()) {
                Room newRoom = new Room(0, roomNumber, roomType, false, 0, "available");
                if (roomDAO.addRoom(newRoom)) {
                    refreshTables();
                    JOptionPane.showMessageDialog(this, "Room added successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add room", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void editRoom() {
        int selectedRow = roomsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int roomId = (int) roomsTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) roomsTable.getValueAt(selectedRow, 3);
        boolean isOccupied = !currentStatus.equals("available");

        JPanel panel = new JPanel(new GridLayout(0, 1));
        String[] statuses = isOccupied ? 
            new String[]{"occupied", "maintenance"} :
            new String[]{"available", "maintenance"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setSelectedItem(currentStatus);

        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);

        // Add deallocate option if room is occupied
        if (isOccupied) {
            JButton deallocateButton = new JButton("Deallocate Room");
            deallocateButton.addActionListener(e -> {
                String remarks = JOptionPane.showInputDialog(this, "Enter remarks for deallocation:");
                if (remarks != null) {
                    if (roomDAO.deallocateRoom(roomId, remarks)) {
                        refreshTables();
                        JOptionPane.showMessageDialog(this, "Room deallocated successfully!");
                        Window win = SwingUtilities.getWindowAncestor(panel);
                        if (win != null) win.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to deallocate room", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            panel.add(deallocateButton);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Room",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newStatus = (String) statusCombo.getSelectedItem();
            if (roomDAO.updateRoomStatus(roomId, newStatus, newStatus.equals("occupied"), null)) {
                refreshTables();
                JOptionPane.showMessageDialog(this, "Room updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update room", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRoomHistory() {
        int selectedRow = roomsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int roomId = (int) roomsTable.getValueAt(selectedRow, 0);
        List<Map<String, Object>> history = roomDAO.getAllocationHistory(roomId);

        // Create history dialog
        JDialog historyDialog = new JDialog(this, "Room Allocation History", true);
        historyDialog.setSize(600, 400);
        historyDialog.setLocationRelativeTo(this);

        // Create history table
        String[] columns = {"Occupant", "Allocation Date", "Checkout Date", "Status", "Remarks"};
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = new JTable(historyModel);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        // Add history data
        for (Map<String, Object> record : history) {
            historyModel.addRow(new Object[]{
                record.get("user_name"),
                formatDate((Timestamp) record.get("allocation_date")),
                formatDate((Timestamp) record.get("checkout_date")),
                record.get("status"),
                record.get("remarks")
            });
        }

        historyDialog.add(scrollPane);
        historyDialog.setVisible(true);
    }

    private void handleRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int requestId = (int) requestsTable.getValueAt(selectedRow, 0);
        int userId = (int) requestsTable.getValueAt(selectedRow, 1);
        String currentStatus = (String) requestsTable.getValueAt(selectedRow, 3);
        String preferredCheckout = (String) requestsTable.getValueAt(selectedRow, 5);

        if (!currentStatus.equals("pending")) {
            JOptionPane.showMessageDialog(this, "This request has already been processed.");
            return;
        }

        // Show available rooms of requested type
        String preferredRoomType = (String) requestsTable.getValueAt(selectedRow, 2);
        List<Room> availableRooms = roomDAO.getAvailableRooms();
        List<Room> matchingRooms = new ArrayList<>();
        for (Room room : availableRooms) {
            if (room.getRoomType().equals(preferredRoomType)) {
                matchingRooms.add(room);
            }
        }

        if (matchingRooms.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "No matching rooms available. Would you like to reject the request?",
                    "No Rooms Available",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                String remarks = JOptionPane.showInputDialog(this, "Enter remarks for rejection:");
                if (remarks != null) {
                    requestDAO.updateRequestStatus(requestId, "rejected", remarks);
                    refreshTables();
                }
            }
            return;
        }

        // Show room selection dialog
        Room selectedRoom = (Room) JOptionPane.showInputDialog(this,
                "Select a room to allocate:",
                "Allocate Room",
                JOptionPane.QUESTION_MESSAGE,
                null,
                matchingRooms.toArray(),
                matchingRooms.get(0));

        if (selectedRoom != null) {
            // Use preferred checkout date from request
            try {
                Date checkoutDate;
                if (!preferredCheckout.equals("-")) {
                    checkoutDate = dateFormat.parse(preferredCheckout);
                } else {
                    // If no preferred date, ask for one
                    String checkoutDateStr = JOptionPane.showInputDialog(this,
                            "Enter expected checkout date (yyyy-MM-dd):",
                            new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    
                    checkoutDate = new SimpleDateFormat("yyyy-MM-dd").parse(checkoutDateStr);
                }

                if (roomDAO.allocateRoom(selectedRoom.getRoomId(), userId, checkoutDate)) {
                    requestDAO.updateRequestStatus(requestId, "approved", "Room " + selectedRoom.getRoomNumber() + " allocated");
                    refreshTables();
                    JOptionPane.showMessageDialog(this, "Room allocated successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to allocate room", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid date format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String formatDate(Timestamp timestamp) {
        return timestamp != null ? dateFormat.format(timestamp) : "-";
    }

    private void logout() {
        new LoginWindow().setVisible(true);
        this.dispose();
    }
}