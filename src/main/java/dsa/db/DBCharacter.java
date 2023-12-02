package dsa.db;

import dsa.character.Character;
import dsa.character.ModifiableValue;
import dsa.character.Profile;
import dsa.services.CharacterComparator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBCharacter {
    private CharacterComparator comparator = CharacterComparator.singleton();

    private DBCharacter() {}
    private static DBCharacter exemplar = null;
    public static DBCharacter singleton() {
        if (exemplar == null) {
            exemplar = new DBCharacter();
        }
        return exemplar;
    }

    public Character loadCharacter(int id) {
        ArrayList<ModifiableValue> values = getModifiedValues(id);
        Profile profile = getProfile(id);
        Character character = new Character(id, values, profile);
        return character;
    }


    public Profile getProfile(int id) {
        Profile wanted = new Profile();
        try {
            Connection conn = DBConnection.getConnection();
            String[] wantedAttribute = {"*"};
            String[] attribute = {"CharacterID"};
            String[] coparator = {"="};
            Object[] values = {id};
            Tabelle table = new Tabelle("\"Character\".\"_Uebersicht\"");
            String query = table.selectWhere(wantedAttribute, attribute, coparator, values);
            System.out.println(query);
            Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(query);

            while(rs.next()){
                wanted.name = StringChecker.convertNullOrEmpty(rs.getString("Name"));
                wanted.geschlecht = StringChecker.convertNullOrEmpty(rs.getString("Geschlecht"));
                wanted.tsaTag = StringChecker.convertNullOrEmpty(rs.getString("TsaTag"));
                wanted.spezies = StringChecker.convertNullOrEmpty(rs.getString("Spezies"));
                wanted.kultur = StringChecker.convertNullOrEmpty(rs.getString("Kultur"));
                wanted.profession = StringChecker.convertNullOrEmpty(rs.getString("Profession"));
                wanted.haarfarbe = StringChecker.convertNullOrEmpty(rs.getString("Haarfarbe"));
                wanted.augenfarbe = StringChecker.convertNullOrEmpty(rs.getString("Augenfarbe"));
                wanted.schamhaare = StringChecker.convertNullOrEmpty(rs.getString("Schamhaare"));
                wanted.brueste = StringChecker.convertNullOrEmpty(rs.getString("Brueste"));
                wanted.genital = StringChecker.convertNullOrEmpty(rs.getString("Genital"));
                wanted.alter = rs.getInt("Alter");
                wanted.groesse = rs.getInt("Groesse");
                wanted.gewicht = rs.getInt("Gewicht");
                wanted.titel = StringChecker.convertNullOrEmpty(rs.getString("Titel"));
                wanted.portrait = StringChecker.convertNullOrEmpty(rs.getString("portrait"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wanted;
    }

    public ArrayList<ModifiableValue> getModifiedValues(int id) {
        ArrayList<ModifiableValue> wanted = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            Tabelle table = new Tabelle("\"Character\".\"" + id + "_ModifiableValues\"");
            String query = table.selectAll();
            Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(query);
            while(rs.next()){
                ModifiableValue mv = new ModifiableValue();
                mv.category = rs.getString("category");
                mv.modified = rs.getString("modified");
                mv.modifier = rs.getString("modifier");
                mv.modValue = rs.getFloat("modValue");
                mv.sumMod = rs.getBoolean("sumMod");
                wanted.add(mv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wanted;
    }

    public String addModifiedValues(int id, ArrayList<ModifiableValue> modifiableValues) {
        String meldung = null;
        for (ModifiableValue m : modifiableValues) {
            String[] attributes = {"category", "modified", "modifier", "modValue", "sumMod"};
            Object[] values = {m.category, m.modified, m.modifier, m.modValue, m.sumMod};
            int[] pk = {1,2};
            Tabelle table = new Tabelle("\"Character\".\"" + id + "_ModifiableValues\"");
            String query = table.insertInto(attributes, values, pk);
            Connection conn = DBConnection.getConnection();
            System.out.println(query);
            try {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
                meldung = e.getMessage();
            }
        }
        return meldung;
    }

    public String deleteModifiedValues(int id, ArrayList<ModifiableValue> modifiableValues) {
        String meldung = null;
        for (ModifiableValue m : modifiableValues) {
            Connection conn = DBConnection.getConnection();
            String[] attributes = {"modified", "modifier"};
            String[] comparator = {"=", "="};
            Object[] values = {m.modified, m.modifier};
            Tabelle table = new Tabelle("\"Character\".\"" + id + "_ModifiableValues\"");
            String query = table.deleteWhere(attributes, comparator, values);
            System.out.println(query);
            try {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
                meldung = e.getMessage();
            }
        }
        return meldung;
    }

    public ArrayList<String> saveCharacter(Character character) {
        ArrayList<String> meldungen = new ArrayList<>();
        ArrayList<ModifiableValue> dbMV = getModifiedValues(character.getId());

        ArrayList[] lists = comparator.compareModifiableValues(character.getValues(), dbMV);
        ArrayList<ModifiableValue> newValues = lists[0];
        ArrayList<ModifiableValue> updatedValues = lists[1];
        ArrayList<ModifiableValue> deletedValues = lists[2];

        if (deletedValues.size() == dbMV.size()) {
            System.out.println("Fehler: alle Werte sollen gelöscht werden!");
            meldungen.add("Fehler: alle Werte sollen gelöscht werden!");
        } else {
            // combine new and updated values into one list
            for (ModifiableValue mv : newValues) {
                updatedValues.add(mv);
            }

            String meldung1 = addModifiedValues(character.getId(), updatedValues);
            if (meldung1 != null) {
                meldungen.add(meldung1);
            }
            String meldung2 = deleteModifiedValues(character.getId(), deletedValues);
            if (meldung2 != null) {
                meldungen.add(meldung2);
            }
        }
        return meldungen;
    }


    public String createModifiableValuesQuery(int charId) {
        String query = "create table \"Character\".\"" + charId + "_ModifiableValues\"\n" +
                "(\n" +
                "    category   varchar,\n" +
                "    modified   varchar          not null,\n" +
                "    modifier   varchar          not null,\n" +
                "    \"modValue\" double precision not null,\n" +
                "    \"sumMod\"   boolean,\n" +
                "    constraint \"" + charId + "_ModifiableValues_pk\"\n" +
                "        primary key (modified, modifier)\n" +
                ");\n" +
                "alter table \"Character\".\"" + charId + "_ModifiableValues\"\n" +
                "    owner to postgres;";
        return query;
    }

    public static void main(String[] args) {
        DBCharacter db = DBCharacter.singleton();

    }
}
