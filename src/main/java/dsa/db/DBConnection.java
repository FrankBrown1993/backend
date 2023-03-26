package dsa.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    protected static Connection con;
    static final String DB_URL = "jdbc:postgresql://h3004666.stratoserver.net:5432/dsadatabase";
    static final String USER = "postgres";
    static final String PASS = "addt3568";

    private static Connection init() {
        try {
            con = DriverManager.getConnection(DB_URL, USER, PASS);
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                return init();
            } else {
                return con;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return init();
        }
    }

    public static void closeConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
