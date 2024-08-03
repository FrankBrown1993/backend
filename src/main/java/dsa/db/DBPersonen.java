package dsa.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBPersonen {
    private DBPersonen() {}
    private static DBPersonen exemplar = null;
    public static DBPersonen singleton() {
        if (exemplar == null) {
            exemplar = new DBPersonen();
        }
        return exemplar;
    }

    public String addPerson(String name, String beschreibung, String meisterInfos,
                            String letzterOrt, String wahrerOrt, String portrait, int sozialStand) {
        String meldung = null;
        try {
            Connection conn = DBConnection.getConnection();
            String[] attributes = {"Name", "Beschreibung", "Meisterinformationen", "letzterOrt",
                    "wahrerOrt", "portrait", "sozialerStand"};
            Object[] values = {name, beschreibung, meisterInfos, letzterOrt, wahrerOrt, portrait,
                    sozialStand};
            int[] pk = {0};
            Tabelle table = new Tabelle("\"Personen\".\"Personen\"");
            String query = table.insertInto(attributes, values, pk);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            meldung = e.getMessage();
        }
        return meldung;
    }

    public String addOrUpdateBeziehung(String name, int charId, int einstellung, int romantik) {
        String meldung = null;
        try {
            Connection conn = DBConnection.getConnection();
            String[] attributes = {"Name", "charID", "Einstellung", "Romantik"};
            Object[] values = {name, charId, einstellung, romantik};
            int[] pk = {0, 1};
            Tabelle table = new Tabelle("\"Personen\".\"Beziehungen\"");
            String query = table.insertInto(attributes, values, pk);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            meldung = e.getMessage();
        }
        return meldung;
    }
}
