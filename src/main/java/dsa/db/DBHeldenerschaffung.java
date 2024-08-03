package dsa.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DBHeldenerschaffung {
    private static DBHeldenerschaffung exemplar = null;

    public static DBHeldenerschaffung singleton() {
        if (exemplar == null) {
            exemplar = new DBHeldenerschaffung();
        }
        return exemplar;
    }

    public ArrayList<String> getErfahrungsStufen() {
        Tabelle t = new Tabelle("\"katalog\".\"erfahrung\"");
        ArrayList<String> professionen = new ArrayList<>();
        String[] attributes = {"name", "ap"};
        String[] orderBy = {"ap"};
        Connection conn = DBConnection.getConnection();
        String query = t.selectDistinctOrderBy(attributes, orderBy);
        // System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                String name = rs.getString("name");
                professionen.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return professionen;
    }

    public LinkedHashMap<String, Integer> getErfahrung(String name) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        String[] intCcolumns = {"ap", "max_wert_eigenschaft", "max_wert_fertigkeit", "max_wert_kampftechnik", "max_summe_eigenschaftspunkte", "max_anzahl_energie_fertigkeiten", "max_fremd_energie_fertigkeiten"};
        Tabelle t = new Tabelle("\"katalog\".\"erfahrung\"");
        String[] attribute = {"name"};
        String[] comparator = {"="};
        Object[] values = {name.trim()};
        Connection conn = DBConnection.getConnection();
        String query = t.selectAllWhere(attribute, comparator, values);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                for (String c : intCcolumns) {
                    map.put(c, rs.getInt(c));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }


}
