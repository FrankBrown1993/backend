package dsa.db;

import dsa.character.Character;
import dsa.character.ModifiableValue;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DBCharakter {
    Tabelle t_charakter = new Tabelle("\"Charakter\".\"Charakter\"");
    HashMap<Integer, Tabelle> t_ausruestung = new HashMap<>();
    HashMap<Integer, Tabelle> t_beinf_werte = new HashMap<>();
    HashMap<Integer, Tabelle> t_werte = new HashMap<>();
    HashMap<Integer, Tabelle> t_vun = new HashMap<>();
    HashMap<Integer, Tabelle> t_sf = new HashMap<>();
    HashMap<Integer, Tabelle> t_notiz = new HashMap<>();

    private DBCharakter() {
        initialisiereTabellen();
    }

    private static DBCharakter exemplar = null;
    public static DBCharakter singleton() {
        if (exemplar == null) {
            exemplar = new DBCharakter();
        }
        return exemplar;
    }

    private void initialisiereTabellen() {
        for (Integer id : getAlleCharakterIds()) {
            t_ausruestung.put(id, new Tabelle("\"Charakter\".\"Ausruestung_" + id + "\""));
            t_beinf_werte.put(id, new Tabelle("\"Charakter\".\"Beeinflusste_Werte_" + id + "\""));
            t_werte.put(id, new Tabelle("\"Charakter\".\"Werte_" + id + "\""));
            t_vun.put(id, new Tabelle("\"Charakter\".\"VuN_" + id + "\""));
            t_sf.put(id, new Tabelle("\"Charakter\".\"SF_" + id + "\""));
            t_notiz.put(id, new Tabelle("\"Charakter\".\"Notiz_" + id + "\""));
        }
    }

    public ArrayList<Integer> getAlleCharakterIds() {
        ArrayList<Integer> wanted = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            String query = t_charakter.selectAll();
            Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(query);
            while(rs.next()){
                int id = rs.getInt("CharakterID");
                wanted.add(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wanted;
    }


    public ArrayList<String> saveCharacter(Character character) {
        ArrayList<String> errors = new ArrayList<>();

        return errors;
    }


    /**
     *
     * @param id id of the character
     * @param modified what should be modified?
     * @param wert value of modification
     * @param modifier name of modifier
     */
    public synchronized void setBeinfWert(int id, String modified, int wert, String modifier) {
        ArrayList<String> errorMsg = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            String[] colNames = {"Modifiziertes", "Mod-Name", "Mod-Wert", "KR"};
            Object[] colValues = {modified, modifier, wert, 0};
            int[] pk = {0, 1};
            String query = t_beinf_werte.get(id).insertInto(colNames, colValues, pk);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);

        } catch (SQLException e) {
            errorMsg.add(e.getMessage());
        }
    }

    public ArrayList<ModifiableValue> convertToModifiableValues(int id) {

        ArrayList<ModifiableValue> values = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            String[] orderedBy = {"Kategorie", "Name"};
            String[] orderType = {"ASC", "ASC"};
            String query = t_werte.get(id).selectAllOrdered(orderedBy, orderType);
            System.out.println(query);
            Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(query);

            while(rs.next()){
                ModifiableValue mv = new ModifiableValue();
                mv.category = rs.getString("Kategorie");
                mv.modified = rs.getString("Name");
                mv.modifier = "standard";
                mv.modValue = rs.getInt("Wert");
                mv.sumMod = true;
                values.add(mv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            Connection conn = DBConnection.getConnection();
            String query = t_beinf_werte.get(id).selectAll();
            System.out.println(query);
            Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(query);

            while(rs.next()){
                ModifiableValue mv = new ModifiableValue();
                mv.modified = rs.getString("Modifiziertes");
                mv.modifier = rs.getString("Mod-Name");

                String wertString = rs.getString("Mod-Wert");
                if (wertString.startsWith("x")) {
                    mv.modValue = Float.valueOf(wertString.substring(1));
                    mv.sumMod = false;
                } else {
                    mv.modValue = Float.valueOf(wertString);
                    mv.sumMod = true;
                }

                if (mv.modifier.equals("standart")) {
                    mv.modifier = "standard";
                    for (ModifiableValue m : values) {
                        if (m.modified.equals(mv.modified)) {
                            if (mv.sumMod) {
                                m.modValue += mv.modValue;
                            } else {
                                m.modValue *= mv.modValue;
                            }
                        }
                    }
                } else {
                    for (ModifiableValue m : values) {
                        if (m.modified.equals(mv.modified)) {
                            mv.category = m.category;
                        }
                    }
                    values.add(mv);
                }


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }

    public static void main(String[] args) {
        DBCharakter db = DBCharakter.singleton();
        DBCharacter dbc = DBCharacter.singleton();
        int id = 5;
        ArrayList<ModifiableValue> values = db.convertToModifiableValues(id);
        dbc.addModifiedValues(id, values);
        for (ModifiableValue m : values) {
            if (m.sumMod) {
                System.out.println(id + ": " + m.category + "/" + m.modified + ": " + m.modValue + " - " + m.modifier);
            } else {
                System.out.println(id + ": " + m.category + "/" + m.modified + ": x" + m.modValue + " - " + m.modifier);
            }
        }
    }
}
