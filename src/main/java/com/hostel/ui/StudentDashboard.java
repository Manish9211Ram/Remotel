package com.hostel.ui;

import com.hostel.dao.RoomDAO;
import com.hostel.dao.RoomRequestDAO;
import com.hostel.model.Room;
import com.hostel.model.RoomRequest;
import com.hostel.model.User;
//  javac -cp "lib/*;bin" -d bin src/main/java/com/hostel/ui/StudentDashboard.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class StudentDashboard extends JFrame {
    private final User student;
    private final RoomDAO roomDAO;
    private final RoomRequestDAO requestDAO;
    private JTable roomsTable;
    private JTable requestsTable;
    private DefaultTableModel roomsModel;
    private DefaultTableModel requestsModel;

    public StudentDashboard(User student) {
        this.student = student;
        this.roomDAO = new RoomDAO();
        this.requestDAO = new RoomRequestDAO();

        setTitle("Student Dashboard - Remotel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Top panel with welcome message
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
        JLabel welcomeLabel = new JLabel("Welcome, " + student.getName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(welcomeLabel, BorderLayout.WEST);

        return panel;
    }

    private JSplitPane createSplitPane() {
        // Available Rooms table
        roomsModel = new DefaultTableModel(
            new Object[]{"Room Number", "Type", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomsTable = new JTable(roomsModel);
        JScrollPane roomsScrollPane = new JScrollPane(roomsTable);
        roomsScrollPane.setBorder(BorderFactory.createTitledBorder("Available Rooms"));

        // My Requests table
        requestsModel = new DefaultTableModel(
            new Object[]{"Request ID", "Room Type", "Status", "Date", "Remarks"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        requestsTable = new JTable(requestsModel);
        JScrollPane requestsScrollPane = new JScrollPane(requestsTable);
        requestsScrollPane.setBorder(BorderFactory.createTitledBorder("My Room Requests"));

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, roomsScrollPane, requestsScrollPane);
        splitPane.setResizeWeight(0.5);

        return splitPane;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);

        JButton requestRoomButton = new JButton("Request Room");
        requestRoomButton.setBackground(new Color(37, 99, 235)); // bg-blue-600
        requestRoomButton.setForeground(Color.BLACK);
        requestRoomButton.setFocusPainted(false);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(220, 38, 38)); // bg-red-600
        logoutButton.setForeground(Color.BLACK);
        logoutButton.setFocusPainted(false);

        panel.add(requestRoomButton);
        panel.add(logoutButton);

        requestRoomButton.addActionListener(e -> showRequestDialog());
        logoutButton.addActionListener(e -> logout());

        return panel;
    }

    private void refreshTables() {
        // Clear existing data
        roomsModel.setRowCount(0);
        requestsModel.setRowCount(0);

        // Load available rooms
        List<Room> rooms = roomDAO.getAvailableRooms();
        for (Room room : rooms) {
            roomsModel.addRow(new Object[]{
                room.getRoomNumber(),
                room.getRoomType(),
                room.getStatus()
            });
        }

        // Load student's requests
        List<RoomRequest> requests = requestDAO.getRequestsByUser(student.getId());
        for (RoomRequest request : requests) {
            requestsModel.addRow(new Object[]{
                request.getRequestId(),
                request.getPreferredRoomType(),
                request.getStatus(),
                request.getRequestDate(),
                request.getRemarks()
            });
        }
    }

    private void showRequestDialog() {
        JComboBox<String> roomTypeCombo = new JComboBox<>(new String[]{"Single", "Double", "Triple"});
        JTextArea remarksArea = new JTextArea(3, 20);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);

        // Create date field with default value (3 months from now)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 3);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        JTextField checkoutDateField = new JTextField(dateFormat.format(cal.getTime()));

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Preferred Room Type:"));
        panel.add(roomTypeCombo);
        panel.add(new JLabel("Preferred Checkout Date (yyyy-MM-dd):"));
        panel.add(checkoutDateField);
        panel.add(new JLabel("Additional Remarks:"));
        panel.add(remarksScroll);

        int result = JOptionPane.showConfirmDialog(this, panel, "Request Room",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
        if (result == JOptionPane.OK_OPTION) {
            String roomType = (String) roomTypeCombo.getSelectedItem();
            String remarks = remarksArea.getText();
            
            try {
                Date checkoutDate = dateFormat.parse(checkoutDateField.getText());
                // Validate checkout date is in the future
                if (checkoutDate.before(new Date())) {
                    JOptionPane.showMessageDialog(this, 
                        "Checkout date must be in the future", 
                        "Invalid Date", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                RoomRequest request = new RoomRequest(0, student.getId(), roomType, "pending", null, remarks);
                request.setPreferredCheckoutDate(checkoutDate);
                
                if (requestDAO.createRequest(request)) {
                    refreshTables();
                    JOptionPane.showMessageDialog(this, "Room request submitted successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to submit request", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid date format. Please use yyyy-MM-dd", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        new LoginWindow().setVisible(true);
        this.dispose();
    }
}