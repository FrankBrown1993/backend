package dsa.db;

import io.vertx.core.json.JsonObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBAbenteuer {
    private static String gruppe = "2";
    Tabelle t_akt_Grp = new Tabelle("\"Abenteuer\".\"Aktuell\"");
    Tabelle t_abentuer = new Tabelle("\"Abenteuer\".\"Abenteuer\"");

    private DBAbenteuer() {}

    private static DBAbenteuer exemplar = null;
    public static DBAbenteuer singleton() {
        if (exemplar == null) {
            exemplar = new DBAbenteuer();
        }
        gruppe = "" + exemplar.getAktuelleGruppe();
        return exemplar;
    }

    public int getAktuelleGruppe() {
        int gruppe = -1;
        try {
            Connection conn = DBConnection.getConnection();
            String query = t_akt_Grp.selectAll();
            System.out.println(query);
            Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(query);
            while(rs.next()) {
                gruppe = rs.getInt("Gruppe");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gruppe;
    }

    public JsonObject getAbenteuerInfos(JsonObject json) {
        this.getAktuelleGruppe();
        try {
            Connection conn = DBConnection.getConnection();
            String[] wantedAttribute = {"*"};
            String[] attribute = {"Gruppe"};
            String[] coparator = {"="};
            Object[] values = {gruppe};
            String query = t_abentuer.selectWhere(wantedAttribute, attribute, coparator, values);
            System.out.println(query);
            Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(query);
            while(rs.next()){
                json.put("region", rs.getString("region"));
                json.put("datum", rs.getString("datum"));
                json.put("bewoelkung", rs.getString("bewoelkung"));
                json.put("aktTemp", rs.getString("aktTemp"));
                json.put("niederschlag", rs.getString("niederschlag"));
                json.put("mond", rs.getInt("mond"));
                json.put("stunde", rs.getInt("stunde"));
                json.put("minute", rs.getInt("minute"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json;
    }
}
