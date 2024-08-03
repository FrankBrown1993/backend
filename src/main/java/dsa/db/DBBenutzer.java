package dsa.db;

import java.sql.*;
import java.util.ArrayList;

public class DBBenutzer {
    private static DBBenutzer exemplar = null;
    public static DBBenutzer singleton() {
        if (exemplar == null) {
            exemplar = new DBBenutzer();
        }
        return exemplar;
    }

    public int getIdVonBenutzer(String name) {
        int id = -1;
        try {
            Connection conn = DBConnection.getConnection();
            String[] wantedAttribute = {"id"};
            String[] attribute = {"Name"};
            String[] coparator = {"="};
            Object[] values = {name};
            Tabelle table = new Tabelle("\"Benutzer\".\"Benutzer\"");
            String query = table.selectWhere(wantedAttribute, attribute, coparator, values);
            Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(query);
            while(rs.next()){
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
}
