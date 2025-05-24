package com.hostel.util;

import com.hostel.dao.UserDAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static final String DB_FILE = "hostel.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;
    
    static {
        // Initialize database on class load
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    name TEXT NOT NULL,
                    email TEXT
                )
            """);

            // Create Rooms table with additional fields
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    room_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_number TEXT UNIQUE NOT NULL,
                    room_type TEXT NOT NULL,
                    is_occupied BOOLEAN DEFAULT FALSE,
                    occupant_id INTEGER,
                    status TEXT DEFAULT 'available',
                    allocation_date TIMESTAMP,
                    expected_checkout_date TIMESTAMP,
                    FOREIGN KEY (occupant_id) REFERENCES users(id)
                )
            """);

            // Create RoomRequests table with preferred_checkout_date
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS room_requests (
                    request_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    preferred_room_type TEXT NOT NULL,
                    status TEXT DEFAULT 'pending',
                    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    remarks TEXT,
                    preferred_checkout_date TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create RoomAllocationHistory table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS room_allocation_history (
                    allocation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    allocation_date TIMESTAMP NOT NULL,
                    checkout_date TIMESTAMP,
                    status TEXT NOT NULL,
                    remarks TEXT,
                    FOREIGN KEY (room_id) REFERENCES rooms(room_id),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create default admin user if not exists
            UserDAO userDAO = new UserDAO();
            userDAO.createAdmin("admin", "admin123", "System Administrator", "admin@hostel.com");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}