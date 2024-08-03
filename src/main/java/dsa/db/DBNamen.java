package dsa.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DBNamen {
    String[] spalten = {"Region", "Namensart", "Eintrag"};
    Random random;

    private static DBNamen exemplar = null;

    private DBNamen() {
        random = new Random();
        random.setSeed(123456789);
    }

    public static DBNamen singleton() {
        if (exemplar == null) {
            exemplar = new DBNamen();
        }
        return exemplar;
    }


    public ArrayList<String> getNamenRegions() {
        Tabelle table = new Tabelle("\"Namen\".\"Namen\"");
        String[] selectedColumns = {"Region"};
        ArrayList<String> wanted = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            String query = table.selectDistinct(selectedColumns);
            // System.out.println(query);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                wanted.add(rs.getString("Region"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wanted;
    }

    public ArrayList<String> getNamensartenOfRegion(String region) {
        Tabelle table = new Tabelle("\"Namen\".\"Namen\"");
        String[] selectedColumns = {"Namensart"};
        String[] attribute = {"Region"};
        String[] coparator = {"="};
        Object[] values = {region};
        ArrayList<String> wanted = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            String query = table.selectDistinctWhere(selectedColumns, attribute, coparator, values);
            // System.out.println(query);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                wanted.add(rs.getString("Namensart"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wanted;
    }

    public ArrayList<String> getNamenOfRegionAndNamensart(String region, String namensart) {
        Tabelle table = new Tabelle("\"Namen\".\"Namen\"");
        String[] selectedColumns = {"Eintrag"};
        String[] attribute = {"Region", "Namensart"};
        String[] coparator = {"=", "="};
        Object[] values = {region, namensart};
        ArrayList<String> wanted = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            String query = table.selectDistinctWhere(selectedColumns, attribute, coparator, values);
            // System.out.println(query);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                wanted.add(rs.getString("Eintrag"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wanted;
    }

    public ArrayList<String> getNamenOfSpecialAttributes(String[] selectedColumns,
                                                         String[] attribute,
                                                         String[] coparator,
                                                         Object[] values) {
        Tabelle table = new Tabelle("\"Namen\".\"Namen\"");
        ArrayList<String> wanted = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            String query = table.selectDistinctWhere(selectedColumns, attribute, coparator, values);
            // System.out.println(query);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                wanted.add(rs.getString("Eintrag"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wanted;
    }

    public String getRandomName(String region) {
        String name = "";
        ArrayList<String> types = getNamensartenOfRegion(region);
        ArrayList<String> vornamen = getNamenOfRegionAndNamensart(region, "Vornamen");
        if (types.contains("Beinamen")) {
            ArrayList<String> beinamen = getNamenOfRegionAndNamensart(region, "Beinamen");
            int r1 = (int)Math.round(Math.random() * vornamen.size() - 1);
            int r2 = (int)Math.round(Math.random() * beinamen.size() - 1);
            // System.out.println("r1: " + r1 +   " (" + vornamen.size() + ")" + ", r2: " + r2 + " (" + beinamen.size() + ")");
            name = vornamen.get(r1) + " " + beinamen.get(r2);
        }

        return name;
    }

    public String getRandomName(String region, boolean female, boolean male) {
        ArrayList<String> orderedTypes = new ArrayList<>(Arrays.asList("Beinamen (vorangestellt)", "Vornamen",
                "Zweitnamen","Beinamen","Nachnamen","Stammesnamen"));
        ArrayList<String> orderedTypesAdel = new ArrayList<>(Arrays.asList("Beinamen (vorangestellt)", "Vornamen Adel",
                "Zweitnamen Adel","Beinamen","Nachnamen Adel","Stammesnamen"));
        ArrayList<Float> probabilities = new ArrayList<>(Arrays.asList(0.5f, 1.f, 0.5f, 0.25f, 0.95f, 0.5f));
        /// ArrayList<Float> probabilities = new ArrayList<>(Arrays.asList(1.f, 1.f, 1.f, 1.f, 1.f, 1.f));
        String fullName = "";

        String[] selectedColumns = {"Eintrag"};
        String[] attribute = {"Region", "Namensart", "male", "female", "prefix", "infix", "postfix"};
        String[] comparator = {"=","=","=","=","=","=","="};
        Object[] values = {region, "placeholder", male, female,false,false,false};
        ArrayList<String> types = getNamensartenOfRegion(region);
        ArrayList<String> typesInOrder = new ArrayList<>();
        Object[] valuesW = {region, "Vornamen", false, true,false,false,false};
        Object[] valuesM = {region, "Vornamen", true, false,false,false,false};
        Object[] valuesN = {region, "Vornamen", true, true,false,false,false};
        ArrayList<String> alleVornamenW = getNamenOfSpecialAttributes(selectedColumns, attribute, comparator, valuesW);
        ArrayList<String> alleVornamenM = getNamenOfSpecialAttributes(selectedColumns, attribute, comparator, valuesM);
        ArrayList<String> alleVornamenN = getNamenOfSpecialAttributes(selectedColumns, attribute, comparator, valuesN);
        ArrayList<String> alleVornamenWMN = new ArrayList<>();
        for (String s : alleVornamenW) {
            alleVornamenWMN.add(s);
        }
        for (String s : alleVornamenM) {
            alleVornamenWMN.add(s);
        }
        for (String s : alleVornamenN) {
            alleVornamenWMN.add(s);
        }
        int index = 0;
        for (String t : orderedTypes) {
            if (types.contains(t) && Math.random() <= probabilities.get(index)) {
                typesInOrder.add(t);
            }
            index++;
        }

        for (String newType : typesInOrder) {
            String name = enlongateNameWith(selectedColumns, attribute, comparator,
                    newType, values, alleVornamenW, alleVornamenM, alleVornamenWMN);
            if (name.length() > 0) {
                if (!fullName.endsWith("-")) {
                    fullName += " " + name;
                } else {
                    fullName += name;
                }
                fullName = fullName.trim();
            }
        }

        return fullName;

    }

    private String enlongateNameWith(String[] selectedColumns,
                                     String[] attribute,
                                     String[] comparator,
                                     String newNamePart,
                                     Object[] initialList,
                                     ArrayList<String> alleNamenW,
                                     ArrayList<String> alleNamenM,
                                     ArrayList<String> alleNamenN) {
        Object[] values = {initialList[0], newNamePart, initialList[2], initialList[3], initialList[4], initialList[5], initialList[6]};
        Object[] initialBackup = new Object[initialList.length];
        Object[] valuesBackup = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            valuesBackup[i] = values[i];
        }
        for (int i = 0; i < initialList.length; i++) {
            initialBackup[i] = initialList[i];
        }
        ArrayList<String> nameList = new ArrayList<>();
        String fullName = "";

        printValues(values, nameList);

        if (Math.random() <= 0.33) { // baue Namen zusammen
            // System.out.println("  baue namen zusammen");
            // prefix
            values[4] = true;
            nameList = getList(initialList, nameList, selectedColumns, attribute, comparator, values);
            if (nameList.size() > 0) {
                String prefix = getRandomOfList(nameList);

                String infix = "";
                double decr = 0;
                for (double d = 1; Math.random() <= d; d-= decr) {  // infix
                    values[4] = false;
                    values[5] = true;
                    values[6] = true;
                    nameList = getList(initialList, nameList, selectedColumns, attribute, comparator, values);
                    if (nameList.size() > 0) {
                        infix += getRandomOfList(nameList);
                    }
                    decr += 0.15 + (1-d);
                }
                values[4] = false;
                values[5] = false;
                values[6] = true;
                String postfix = "";
                nameList = getList(initialList, nameList, selectedColumns, attribute, comparator, values);
                if (nameList.size() > 0) {
                    postfix = getRandomOfList(nameList);
                }
                fullName = prefix + infix + postfix;
            }

        }

        if (fullName.length() == 0) { // nehme kompletten Namen aus der Liste
            // System.out.println("  nehme fertigen namen");
            values = valuesBackup;
            initialList = initialBackup;

            nameList = getList(initialList, nameList, selectedColumns, attribute, comparator, values);


            fullName = getRandomOfList(nameList);
            if (fullName.contains("<NameW>")) {
                String name = getRandomOfList(alleNamenW);
                fullName = fullName.replace("<NameW>", name);
            } else if (fullName.contains("<NameM>")) {
                String name = getRandomOfList(alleNamenM);
                fullName = fullName.replace("<NameM>", name);
            } else if (fullName.contains("<NameN>")) {
                String name = getRandomOfList(alleNamenN);
                fullName = fullName.replace("<NameN>", name);
            }
        }
        return fullName;
    }

    private ArrayList<String> getList(Object[] initialList,
                                      ArrayList<String> nameList,
                                      String[] selectedColumns,
                                      String[] attribute,
                                      String[] comparator,
                                      Object[] values) {
        nameList = getNamenOfSpecialAttributes(selectedColumns, attribute, comparator, values);

        printValues(values, nameList);

        if ((boolean)initialList[2] && (boolean)initialList[3] && nameList.size() == 0) {  // neutral -> male || female
            // System.out.println("  neutral -> male || female");
            int index = random.nextInt(2) + 2 ;
            values[index] = false;
            initialList[index] = false;
            nameList = getNamenOfSpecialAttributes(selectedColumns, attribute, comparator, values);

            printValues(values, nameList);
        } else if (((boolean)initialList[2] || (boolean)initialList[3]) && nameList.size() == 0) {  // male || female -> neutral
            // System.out.println("  male || female -> neutral");
            values[2] = true;
            values[3] = true;
            nameList = getNamenOfSpecialAttributes(selectedColumns, attribute, comparator, values);

            printValues(values, nameList);
        }
        return nameList;
    }

    private void printValues(Object[] values, ArrayList<String> nameList) {
        /*
        System.out.print(values[0] + " -> ");
        for (int i = 1; i < 7; i++) {
            System.out.print(values[i] + " -> ");
        }
        System.out.println();
        System.out.println("nameList.size(): " + nameList.size());

        */
    }

    private String getRandomOfList(ArrayList<String> list) {
        int index = random.nextInt(list.size());
        return list.get(index);
    }
}
