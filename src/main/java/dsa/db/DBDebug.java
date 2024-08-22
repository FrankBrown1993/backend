package dsa.db;

import dsa.db.converter.ProfessionConverter;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class DBDebug {
    private static DBDebug exemplar = null;

    private DBDebug() {}

    public static DBDebug singleton() {
        if (exemplar == null) {
            exemplar = new DBDebug();
        }
        return exemplar;
    }

    public void insertProfessionRaw(HashMap<String, String> map) {
        int size = map.keySet().size();
        Connection conn = DBConnection.getConnection();
        Tabelle t = new Tabelle("\"katalog\".\"professionen\"");
        String[] attributes = new String[size];
        String[] values = new String[size];
        int index = 0;
        int pkIndex = -1;
        for (String key : map.keySet()) {
            if (key.equals("name")) {
                pkIndex = index;
            }
            attributes[index] = key.toLowerCase();
            values[index] = map.get(key);
            index ++;
        }
        int[] pk = {pkIndex};
        String query = t.insertInto(attributes, values, pk);
        // System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JsonObject getProfessionJson(String name) {
        JsonObject json = null;
        Tabelle t = new Tabelle("\"katalog\".\"professionen\"");
        String[] selectedColumns = {"unterteilung", "name", "ap", "beschreibung", "ausruestung", "voraussetzungen", "sonderfertigkeiten", "kampftechniken", "talente_koerper", "talente_gesellschaft", "talente_natur", "talente_wissen", "talente_handwerk", "extraap", "zauber", "zaubertricks", "liturgien", "typischevorteile", "typischenachteile", "untypischevorteile", "untypischenachteile", "publikation", "anmerkung", "errata", "name_w", "name_m"};
        int[] types = {                          3,      1,    0,              1,             1,                 3,                    3,                3,                  3,                     3,               3,                3,                  3,         2,        3,              3,           3,                  3,                   3,                    3,                     3,             1,           1,        1,        1,        1};
        // types: 0: int, 1: String, 2: JsonObject, 3: JsonArray
        // String[] stringColumns = {"name", "beschreibung", "ausruestung", "publikation", "anmerkung", "errata", "name_w", "name_m"};
        // String[] jsonColumns = {"unterteilung", "voraussetzungen", "sonderfertigkeiten", "kampftechniken", "talente_koerper", "talente_gesellschaft", "talente_natur", "talente_wissen", "talente_handwerk", "extraap", "zauber", "zaubertricks", "liturgien", "typischevorteile", "typischenachteile", "untypischevorteile", "untypischenachteile"};
        String[] attributes = {"name"};
        String[] comparators = {"="};
        String[] values = {name};
        Connection conn = DBConnection.getConnection();
        String query = t.selectWhere(selectedColumns, attributes, comparators, values);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            json = new JsonObject();
            while(rs.next()){
                for (int i = 0; i < selectedColumns.length; i++) {
                    String key = selectedColumns[i];
                    switch (types[i]) {
                        case 0:
                            json.put(key, rs.getInt(key));
                            break;
                        case 1:
                            json.put(key, rs.getString(key));
                            break;
                        case 2:
                            JsonObject obj = new JsonObject(rs.getString(key));
                            json.put(key, obj);
                            break;
                        case 3:
                            JsonArray arr = new JsonArray(rs.getString(key));
                            json.put(key, arr);
                            break;
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static void main(String[] args) {
        DBDebug db = DBDebug.singleton();

        DBHeldenerschaffung dbh = DBHeldenerschaffung.singleton();
        ArrayList<String> professionen = dbh.getAlleProfessionsnamen();

        for (String p : professionen) {
            System.out.println(p);
            JsonObject json = dbh.getProfessionJson(p);
            HashMap<String, String> jsonMap = ProfessionConverter.convertJsonToHashMap(json);
            db.insertProfessionRaw(jsonMap);
        }
        /*
        JsonObject json2 = db.getProfessionJson("Achazschamane");
        System.out.println(json);
        System.out.println(json2);
        */
    }
}
