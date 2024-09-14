package dsa.db;

import dsa.db.converter.KulturenConverter;
import dsa.db.converter.ProfessionConverter;
import dsa.db.converter.SpeziesConverter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;

import javax.json.Json;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public class DBHeldenerschaffung {
    private static DBHeldenerschaffung exemplar = null;

    private DBHeldenerschaffung() {}

    public static DBHeldenerschaffung singleton() {
        if (exemplar == null) {
            exemplar = new DBHeldenerschaffung();
        }
        return exemplar;
    }

    public ArrayList<String> getAlleUser() {
        Tabelle t = new Tabelle("\"Benutzer\".\"Benutzer\"");
        ArrayList<String> list = new ArrayList<>();
        String[] attributes = {"id", "Name"};
        String[] orderBy = {"Name"};
        Connection conn = DBConnection.getConnection();
        String query = t.selectDistinctOrderBy(attributes, orderBy);
        // System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                String name = rs.getString("Name");
                list.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public ArrayList<String> getErfahrungsStufen() {
        Tabelle t = new Tabelle("\"katalog\".\"erfahrung\"");
        ArrayList<String> list = new ArrayList<>();
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
                list.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
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

    public JsonObject getErfahrungJson(String name) {
        JsonObject json = new JsonObject();
        LinkedHashMap<String, Integer> erfInfos = getErfahrung(name);
        for (String key : erfInfos.keySet()) {
            int value = erfInfos.get(key);
            json.put(key, value);
        }
        return json;
    }

    public ArrayList<Pair<String, String>> getAlleSpezies() {
        Tabelle t = new Tabelle("\"katalog\".\"spezies\"");
        ArrayList<Pair<String, String>> arrayList = new ArrayList<>();
        String[] attributes = {"art", "unterart"};
        String[] orderBy = {"art", "unterart"};
        Connection conn = DBConnection.getConnection();
        String query = t.selectDistinctOrderBy(attributes, orderBy);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                String art = rs.getString("art");
                String unterart = rs.getString("unterart");
                arrayList.add(new Pair<>(art, unterart));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public String getSpeziesArt(String unterart) {
        Tabelle t = new Tabelle("\"katalog\".\"spezies\"");
        String art = "";
        String[] wantedAttributes = {"art"};
        String[] attributes = {"unterart"};
        String[] comparator = {"="};
        String[] values = {unterart};
        Connection conn = DBConnection.getConnection();
        String query = t.selectWhere(wantedAttributes, attributes, comparator, values);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                art = rs.getString("art");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return art;
    }

    public LinkedHashMap<String, String> getSpeziesInfo(String spezies) {
        LinkedHashMap<String, String> outerMap = new LinkedHashMap<>();
        Tabelle t = new Tabelle("\"katalog\".\"spezies\"");
        String[] wantedAttributes = {"art", "unterart", "apwert", "lepgrundwert", "gsgrundwert", "skgrundwert", "zkgrundwert", "eigenschaftsaenderungen", "koerperbauundaussehen", "herkunftundverbreitung", "vermehrungundalterung", "gewicht", "koerpergroesse", "haarfarbe", "augenfarbe", "automatischevorteile", "dringendempfohlenevorteile", "dringendempfohlenenachteile", "typischevorteile", "typischenachteile", "untypischevorteile", "untypischenachteile", "ueblichekulturen", "restlichekulturen", "begruessung", "verabschiedung", "schwangerschaftsgeschlecht", "schwangerschaftskinderzahl", "brustgroesse", "penisgroesse", "vaginagroesse", "schamhaarmenge", "erwachsenenalter", "lebenserwartung"};
        String[] attribute = {"unterart"};
        String[] comparator = {"="};
        Object[] values = {spezies};
        Connection conn = DBConnection.getConnection();
        String query = t.selectWhere(wantedAttributes, attribute, comparator, values);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                for (String key : wantedAttributes) {
                    String value = rs.getString(key);
                    outerMap.put(key, value);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return outerMap;
    }

    public JsonObject getSpeziesInfoJson(String spezies) {
        HashMap<String, String> map = getSpeziesInfo(spezies);
        JsonObject json = SpeziesConverter.getSpeziesInfoJson(map);
        return json;
    }

    /**
     * kulturart 0: übliche, 1: restliche, 2: unübliche
     * */
    public JsonObject getKulturenOfSpezies(String spezies) {
        JsonObject json = new JsonObject();
        ArrayList<String> kulturenAll = getAlleKulturen();
        Collections.sort(kulturenAll);
        HashMap<String, String> map = getSpeziesInfo(spezies);
        ArrayList<String> kulturen0 = SpeziesConverter.getKulturen(map, "ueblichekulturen");
        ArrayList<String> kulturen1 = SpeziesConverter.getKulturen(map, "restlichekulturen");
        JsonArray k0 = new JsonArray();
        JsonArray k1 = new JsonArray();
        JsonArray k2 = new JsonArray();
        for (String kultur : kulturenAll) {
            if (kulturen0.contains(kultur)) {
                k0.add(kultur);
            } else if (kulturen1.contains(kultur)) {
                k1.add(kultur);
            } else {
                k2.add(kultur);
            }
        }
        json.put("uebliche_kulturen", k0);
        json.put("restliche_kulturen", k1);
        json.put("unuebliche_kulturen", k2);
        return json;
    }


    public ArrayList<String> getAlleKulturen() {
        Tabelle t = new Tabelle("\"katalog\".\"kultur\"");
        ArrayList<String> arrayList = new ArrayList<>();
        String[] attributes = {"name"};
        Connection conn = DBConnection.getConnection();
        String query = t.selectDistinct(attributes);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                String name = rs.getString("name");
                arrayList.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public HashMap<String, String> getKulturInfo(String name) {
        HashMap<String, String> outerMap = new HashMap<>();
        Tabelle t = new Tabelle("\"katalog\".\"kultur\"");
        String[] wantedAttributes = {"name", "beschreibung", "verbreitungundlebensweise", "weltsichtundglaube", "sittenundbraeuche", "trachtundbewaffnung", "sprache", "schrift", "ortskenntnis", "sozialstatus", "typischeprofession", "typischeweltlicheprofession", "typischezaubererprofession", "typischegeweihtenprofession", "typischevorteile", "typischenachteile", "untypischevorteile", "untypischenachteile", "typischetalente", "untypischetalente", "namensinformationen", "paketkosten", "talentmodifikatoren"};
        String[] attribute = {"name"};
        String[] comparator = {"="};
        Object[] values = {name.trim()};
        Connection conn = DBConnection.getConnection();
        String query = t.selectWhere(wantedAttributes, attribute, comparator, values);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                for (String key : wantedAttributes) {
                    String value = rs.getString(key);
                    outerMap.put(key, value);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return outerMap;
    }

    public JsonObject getKulturInfoJson(String kultur) {
        HashMap<String, String> map = getKulturInfo(kultur);
        JsonObject json = KulturenConverter.getKulturInfoJson(map);
        return json;
    }

    public ArrayList<String> getAlleProfessionen() {
        Tabelle t = new Tabelle("\"katalog\".\"professionen\"");
        ArrayList<String> professionen = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String query = t.selectAll();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                JsonArray arr = new JsonArray(rs.getString("unterteilung"));
                String name = arr.getString(0);
                for (int i = 1; i < arr.size(); i++) {
                    name += ">" + arr.getString(i);
                }
                professionen.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return professionen;
    }

    /*
    public ArrayList<String> getAlleProfessionsnamen() {
        Tabelle t = new Tabelle("\"katalog\".\"profession\"");
        ArrayList<String> professionen = new ArrayList<>();
        String[] attributes = {"name"};
        Connection conn = DBConnection.getConnection();
        String query = t.selectDistinct(attributes);
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
    }*/

    public ArrayList<String> getAlleProfessionsnamen() {
        Tabelle t = new Tabelle("\"katalog\".\"professionen\"");
        ArrayList<String> professionen = new ArrayList<>();
        String[] attributes = {"name"};
        Connection conn = DBConnection.getConnection();
        String query = t.selectDistinct(attributes);
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

    public ArrayList<String> getTypischeProfessionen(String kultur) {
        HashMap<String, String> map = getKulturInfo(kultur);
        String[] profKat = {"typischeprofession", "typischeweltlicheprofession",
                "typischezaubererprofession", "typischegeweihtenprofession"};
        ArrayList<String> professionenList = new ArrayList<>();

        for (String prof : profKat) {
            ArrayList<Triplet<String, String, String>> profList =
                    KulturenConverter.getProfessionen(map, prof);
            for (Triplet<String, String, String> p : profList) {
                String pName = p.getValue0();
                if (p.getValue1() != null && p.getValue1().length() > 0) {
                    pName = p.getValue1();
                }
                ArrayList<String> professions = getProfessionsOfType(pName);
                for (String pEntry : professions) {
                    professionenList.add(pEntry);
                }
            }

        }
        return professionenList;
    }

    /*
    public ArrayList<String> getProfessionsOfTypeDepr(String type) {
        ArrayList<String> professions = new ArrayList<>();
        String[] attribute = {"name", "oberkategorie", "unterkategorie", "eintrag"};
        String[] values = {"", "Unterteilung", "", escapeRegex(type)};

        ArrayList<Quartet<String, String, String, String>> list = getRegex(attribute, values);
        for (Quartet<String, String, String, String> q : list) {
            professions.add(q.getValue3());
        }
        return professions;
    }*/

    public ArrayList<String> getProfessionsOfType(String type) {
        Tabelle t = new Tabelle("\"katalog\".\"professionen\"");
        String[] wantedAttribute = {"unterteilung"};
        String[] attribute = {"unterteilung"};
        String[] values = {"%" + type + "%"};
        ArrayList<String> professionen = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String query = t.selectWhereLike(wantedAttribute, attribute, values);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                JsonArray arr = new JsonArray(rs.getString("unterteilung"));
                String name = arr.getString(0);
                for (int i = 1; i < arr.size(); i++) {
                    name += ">" + arr.getString(i);
                }
                professionen.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return professionen;
    }

    public JsonObject getAlleProfessionenJsonDepr(int type, String kultur) {
        ArrayList<String> professionen;
        if (type > 0 ){
            professionen = getTypischeProfessionen(kultur);
        } else {
            professionen = getAlleProfessionen();
        }

        Collections.sort(professionen);

        JsonObject professionenJson = new JsonObject();

        for (String p : professionen) {
            String[] split = p.split(Pattern.quote(">"));

            ArrayList<JsonObject> list = new ArrayList<>();

            for (int i = 0; i < split.length; i++) {
                JsonObject subBranch;
                if (i == 0) {
                    subBranch = professionenJson.getJsonObject(split[i]);
                } else {
                    subBranch = list.get(i - 1).getJsonObject(split[i]);
                }
                if (subBranch == null) {
                    subBranch = new JsonObject();
                }
                if (i == split.length - 1) {
                    subBranch.put("name", split[i]);
                }
                list.add(subBranch);
            }

            for (int i = list.size() - 2; i >= 0; i--) {
                list.get(i).put(split[i + 1], list.get(i+1));
            }

            professionenJson.put(split[0], list.get(0));
        }
        return professionenJson;
    }

    public JsonObject getAlleProfessionenJson(int type, String kultur) {
        ArrayList<String> professionen;
        if (type > 0 ){
            professionen = getTypischeProfessionen(kultur);
        } else {
            professionen = getAlleProfessionen();
        }

        Collections.sort(professionen);

        JsonObject professionenJson = new JsonObject();

        for (String p : professionen) {
            String[] split = p.split(Pattern.quote(">"));

            ArrayList<JsonObject> list = new ArrayList<>();

            for (int i = 0; i < split.length; i++) {
                JsonObject subBranch;
                if (i == 0) {
                    subBranch = professionenJson.getJsonObject(split[i]);
                } else {
                    subBranch = list.get(i - 1).getJsonObject(split[i]);
                }
                if (subBranch == null) {
                    subBranch = new JsonObject();
                }
                if (i == split.length - 1) {
                    subBranch.put("name", split[i]);
                }
                list.add(subBranch);
            }

            for (int i = list.size() - 2; i >= 0; i--) {
                list.get(i).put(split[i + 1], list.get(i+1));
            }

            professionenJson.put(split[0], list.get(0));
        }
        return professionenJson;
    }

    /*
    public HashMap<String, HashMap<String, String>> getProfessionInfo(String name) {
        HashMap<String, HashMap<String, String>> outerMap = new HashMap<>();
        Tabelle t = new Tabelle("\"katalog\".\"profession\"");
        String[] wantedAttributes = {"name", "oberkategorie", "unterkategorie", "eintrag"};
        String[] attribute = {"name"};
        String[] comparator = {"="};
        Object[] values = {name.trim()};
        Connection conn = DBConnection.getConnection();
        String query = t.selectWhere(wantedAttributes, attribute, comparator, values);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                String o = rs.getString("oberkategorie");
                String u = rs.getString("unterkategorie");
                String e = rs.getString("eintrag");
                HashMap<String, String> innerMap = outerMap.get(o);
                if (innerMap == null) {
                    innerMap = new HashMap<>();
                }
                innerMap.put(u, e);
                outerMap.put(o, innerMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return outerMap;

    }*/

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

    /*
    public JsonObject getProfessionInfoJson(String profession) {
        HashMap<String, HashMap<String, String>> map = getProfessionInfo(profession);
        JsonObject json = ProfessionConverter.getProfessionInfoJson(profession, map);
        return json;
    }*/

    public JsonObject getAussehenInfoJson(String spezies) {
        JsonObject json = new JsonObject();
        HashMap<String, String> map = getSpeziesInfo(spezies);
        String[] diceWerte = {"haarfarbe", "augenfarbe", "schwangerschaftsgeschlecht", "schwangerschaftskinderzahl", "brustgroesse", "penisgroesse", "vaginagroesse", "schamhaarmenge"};
        for (String dw : diceWerte) {
            JsonObject array = new JsonObject();
            HashMap<Integer, String> diceMap = SpeziesConverter.getDiceThrowList(map, dw);
            for (Integer key : diceMap.keySet()) {
                array.put(key + "", diceMap.get(key));
            }
            json.put(dw, array);
        }
        return json;
    }

    /*
    public void getProfessionenMW() {
        ArrayList<Triplet<String, String, String>> triplets = new ArrayList<>();
        triplets.add(new Triplet<>("schamane", "schamanin", "schamane"));
        triplets.add(new Triplet<>("Amazone", "Amazone", null));
        ArrayList<String> leftover = new ArrayList<>();
        for (String p : getAlleProfessionsnamen()) {
            boolean converted = false;
            for (Triplet<String, String, String> t : triplets)
                if (p.endsWith(t.getValue0())) {
                    System.out.println(p);
                    String prefix = p.substring(0, p.lastIndexOf(t.getValue0()));
                    if (t.getValue1() != null) {
                        System.out.println("  w -> " + prefix + t.getValue1());
                    } else {
                        System.out.println("  w -> KEINE");
                    }
                    if (t.getValue2() != null) {
                        System.out.println("  m -> " + prefix + t.getValue2());
                    } else {
                        System.out.println("  m -> KEINE");
                    }
                    converted = true;
                }
            if (!converted) {
                leftover.add(p);
            }
        }
        System.out.println("\nnicht convertiert:");
        for (String p : leftover) {
            System.out.println(p);
        }
    }*/


    public JsonArray getVuNListJson() {
        JsonArray array = new JsonArray();
        Tabelle t = new Tabelle("\"Glossar\".\"Vorteile_Nachteile\"");
        Tabelle t_kat_kost = new Tabelle("\"Glossar\".\"VuN_Kategorie-Kosten\"");
        Tabelle t_voraus = new Tabelle("\"Glossar\".\"VuN_Voraussetzungen\"");
        Connection conn = DBConnection.getConnection();
        String query = t.selectAll();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                JsonObject json = new JsonObject();
                String name = rs.getString("Name");
                json.put("name", name);
                json.put("beschreibung", rs.getString("Beschreibung"));
                json.put("regel", rs.getString("Regel"));
                json.put("kosten", rs.getInt("AP-Kosten"));
                json.put("stufen", rs.getInt("Stufen"));
                json.put("max_anzahl", rs.getInt("maxAnzahl"));
                json.put("vorteil", rs.getBoolean("Vorteil"));
                json.put("spezifizierung", rs.getBoolean("SpezifizierungNoetig"));
                json.put("art", rs.getString("Art"));
                json.put("kategorie", rs.getString("kategorie"));

                JsonArray kat_kost_list = new JsonArray();
                String[] attributes = {"Name"};
                String[] comparator = {"="};
                String[] values = {name};
                String query_kat_kost = t_kat_kost.selectAllWhere(attributes, comparator, values);
                Statement stmt_kat_kost = conn.createStatement();
                ResultSet rs_kat_kost = stmt_kat_kost.executeQuery(query_kat_kost);
                while(rs_kat_kost.next()){
                    JsonObject kat_kost = new JsonObject();
                    kat_kost.put("kategorie", rs_kat_kost.getString("Kategorie"));
                    kat_kost.put("stufe", rs_kat_kost.getString("Stufe"));
                    kat_kost.put("kosten", rs_kat_kost.getInt("Kosten"));
                    kat_kost_list.add(kat_kost);
                }
                json.put("kategorie_kosten", kat_kost_list);

                JsonArray voraus_list = new JsonArray();
                String query_voraus = t_voraus.selectAllWhere(attributes, comparator, values);
                Statement stmt_voraus = conn.createStatement();
                ResultSet rs_voraus = stmt_voraus.executeQuery(query_voraus);
                while(rs_voraus.next()){
                    voraus_list.add(rs_voraus.getString("Voraussetzung"));
                }
                json.put("voraussetzungen", voraus_list);
                array.add(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }

    public JsonArray getVuNListJsonAlternativeEinzeln() {
        JsonArray vun = new JsonArray();
        Connection conn = DBConnection.getConnection();
        String query = "SELECT * FROM \"Glossar\".\"Vorteile_Nachteile\" " +
                "LEFT JOIN \"Glossar\".\"VuN_Kategorie-Kosten\" " +
                "ON \"Vorteile_Nachteile\".\"Name\" = \"VuN_Kategorie-Kosten\".\"Name\" " +
                "LEFT JOIN \"Glossar\".\"VuN_Voraussetzungen\" " +
                "ON \"Vorteile_Nachteile\".\"Name\" = \"VuN_Voraussetzungen\".\"Name\" " +
                "ORDER BY \"Vorteil\" DESC, \"Vorteile_Nachteile\".\"Name\", \"Stufe\";";
        // System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            LinkedHashMap<String, JsonObject> map = new LinkedHashMap<>();
            while(rs.next()) {
                String name = rs.getString(1);
                JsonObject json = map.get(name);
                if (json == null) {
                    json = new JsonObject();
                    json.put("name", name);
                    json.put("beschreibung", rs.getString(2));
                    json.put("regel", rs.getString(3));
                    json.put("kosten", rs.getInt(4));
                    json.put("stufen", rs.getInt(5));
                    json.put("max_anzahl", rs.getInt(6));
                    json.put("vorteil", rs.getBoolean(7));
                    json.put("spezifizierung", rs.getBoolean(8));
                    json.put("art", rs.getString(9));
                    json.put("kategorie", rs.getString(10));
                }

                JsonArray kat_kost_list = json.getJsonArray("kategorie_kosten");
                if (kat_kost_list == null) {
                    kat_kost_list = new JsonArray();
                }
                String kategorie = rs.getString(12);
                if (kategorie != null && kategorie.trim().length() > 0) {
                    JsonObject kat_kost = new JsonObject();
                    kat_kost.put("kategorie", kategorie);
                    kat_kost.put("stufe", rs.getString(13));
                    kat_kost.put("kosten", rs.getInt(14));
                    if (!kat_kost_list.contains(kat_kost)) {
                        kat_kost_list.add(kat_kost);
                    }
                }
                json.put("kategorie_kosten", kat_kost_list);

                JsonArray voraus_list = json.getJsonArray("voraussetzungen");
                if (voraus_list == null) {
                    voraus_list = new JsonArray();
                }
                String voraussetzung = rs.getString(16);
                if (voraussetzung != null && voraussetzung.trim().length() > 0) {
                    if (!voraus_list.contains(voraussetzung)) {
                        voraus_list.add(voraussetzung);
                    }
                }
                json.put("voraussetzungen", voraus_list);
                map.put(name, json);
            }
            for (String key : map.keySet()) {
                vun.add(map.get(key));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vun;
    }

    public Pair<JsonArray, JsonArray> getVuNListJsonAlternative() {
        JsonArray vorteile = new JsonArray();
        JsonArray nachteile = new JsonArray();
        Connection conn = DBConnection.getConnection();
        String query = "SELECT * FROM \"Glossar\".\"Vorteile_Nachteile\" " +
                "LEFT JOIN \"Glossar\".\"VuN_Kategorie-Kosten\" " +
                "ON \"Vorteile_Nachteile\".\"Name\" = \"VuN_Kategorie-Kosten\".\"Name\" " +
                "LEFT JOIN \"Glossar\".\"VuN_Voraussetzungen\" " +
                "ON \"Vorteile_Nachteile\".\"Name\" = \"VuN_Voraussetzungen\".\"Name\" " +
                "ORDER BY \"Vorteile_Nachteile\".\"Name\", \"Stufe\";";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            LinkedHashMap<String, JsonObject> map = new LinkedHashMap<>();
            while(rs.next()) {
                String name = rs.getString(1);
                JsonObject json = map.get(name);
                if (json == null) {
                    json = new JsonObject();
                    json.put("name", name);
                    json.put("beschreibung", rs.getString(2));
                    json.put("regel", rs.getString(3));
                    json.put("kosten", rs.getInt(4));
                    json.put("stufen", rs.getInt(5));
                    json.put("max_anzahl", rs.getInt(6));
                    json.put("vorteil", rs.getBoolean(7));
                    json.put("spezifizierung", rs.getBoolean(8));
                    json.put("art", rs.getString(9));
                    json.put("kategorie", rs.getString(10));
                }

                JsonArray kat_kost_list = json.getJsonArray("kategorie_kosten");
                if (kat_kost_list == null) {
                    kat_kost_list = new JsonArray();
                }
                String kategorie = rs.getString(12);
                if (kategorie != null && kategorie.trim().length() > 0) {
                    JsonObject kat_kost = new JsonObject();
                    kat_kost.put("kategorie", kategorie);
                    kat_kost.put("stufe", rs.getString(13));
                    kat_kost.put("kosten", rs.getInt(14));
                    if (!kat_kost_list.contains(kat_kost)) {
                        kat_kost_list.add(kat_kost);
                    }
                }
                json.put("kategorie_kosten", kat_kost_list);

                JsonArray voraus_list = json.getJsonArray("voraussetzungen");
                if (voraus_list == null) {
                    voraus_list = new JsonArray();
                }
                String voraussetzung = rs.getString(16);
                if (voraussetzung != null && voraussetzung.trim().length() > 0) {
                    if (!voraus_list.contains(voraussetzung)) {
                        voraus_list.add(voraussetzung);
                    }
                }
                json.put("voraussetzungen", voraus_list);
                map.put(name, json);
            }
            for (String key : map.keySet()) {
                if (map.get(key).getBoolean("vorteil")) {
                    vorteile.add(map.get(key));
                } else {
                    nachteile.add(map.get(key));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Pair<>(vorteile, nachteile);
    }

    public Quintet<JsonArray, JsonArray, JsonArray, JsonArray, JsonArray> getTalenteJson() {
        JsonArray talente_k = new JsonArray();
        JsonArray talente_g = new JsonArray();
        JsonArray talente_n = new JsonArray();
        JsonArray talente_w = new JsonArray();
        JsonArray talente_h = new JsonArray();
        Tabelle t = new Tabelle("\"Glossar\".\"Talent\"");
        Connection conn = DBConnection.getConnection();
        // String query = t.selectAllOrdered(orderedBy, orderType);
        String[] joinSchemas = {"Glossar"};
        String[] joinTables = {"Talent_Anwendungen"};
        String[] joinAttributes = {"Name"};
        String query = t.selectAllLeftJoin("Name", joinSchemas, joinTables, joinAttributes);
        String[] tables = {"Talent","Talent_Anwendungen"};
        String[] orderedBy = {"Name", "Anwendung"};
        String[] orderType = {"ASC", "ASC"};
        query = t.addSort(query, tables, orderedBy, orderType);
        System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            LinkedHashMap<String, JsonObject> map = new LinkedHashMap<>();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String name = rs.getString(1);
                JsonObject json = map.get(name);
                if (json == null) {
                    json = new JsonObject();
                    json.put("name", name);
                    json.put("art", rs.getString(2));
                    json.put("attr_1", rs.getString(3));
                    json.put("attr_2", rs.getString(4));
                    json.put("attr_3", rs.getString(5));
                    json.put("beschreibung", rs.getString(6));
                    json.put("be", rs.getString(7));
                    json.put("steigerung", rs.getString(12));
                    json.put("werkzeug", rs.getString(13));
                }

                JsonArray anwendungen = json.getJsonArray("anwendungen");
                if (anwendungen == null) {
                    anwendungen = new JsonArray();
                }
                String anwendung = rs.getString(15);
                if (anwendung != null && anwendung.trim().length() > 0) {
                    JsonObject anw_json = new JsonObject();
                    anw_json.put("anwendung", anwendung);
                    anw_json.put("beschreibung", rs.getString(16));
                    anw_json.put("ap_kosten", rs.getInt(19));
                    if (!anwendungen.contains(anw_json)) {
                        anwendungen.add(anw_json);
                    }
                }
                json.put("anwendungen", anwendungen);
                map.put(name, json);
            }
            for (String key : map.keySet()) {
                System.out.println(key);
                JsonObject json = map.get(key);
                String art = json.getString("art");
                if (art.equals("körperlich")) {
                    talente_k.add(json);
                } else if (art.equals("gesellschaftlich")) {
                    talente_g.add(json);
                } else if (art.equals("natur")) {
                    talente_n.add(json);
                } else if (art.equals("wissen")) {
                    talente_w.add(json);
                } else if (art.equals("handwerk")) {
                    talente_h.add(json);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Quintet<>(talente_k, talente_g, talente_n, talente_w, talente_h);
    }

    public JsonArray getKampftechnikenJson() {
        JsonArray kampftechniken = new JsonArray();
        Tabelle t = new Tabelle("\"Glossar\".\"Kampftechniken\"");
        Connection conn = DBConnection.getConnection();
        String[] orderedBy = {"Name"};
        String[] orderType = {"ASC"};
        String query = t.selectAllOrdered(orderedBy, orderType);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                JsonObject json = new JsonObject();
                String name = rs.getString("Name");
                json.put("name", name);
                json.put("le", rs.getString("Leiteigenschaft"));
                json.put("steigerung", rs.getString("Steigerung"));
                json.put("bruchfaktor", rs.getInt("Bruchfaktor"));
                json.put("beschreibung", rs.getString("Beschreibung"));
                json.put("besonderheiten", rs.getString("Besonderheiten"));
                json.put("nk", rs.getBoolean("Nahkampf"));
                kampftechniken.add(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kampftechniken;
    }

    public JsonArray getSprachenJson() {
        JsonArray sprachen = new JsonArray();
        Tabelle t = new Tabelle("\"Glossar\".\"Sprachen\"");
        Connection conn = DBConnection.getConnection();
        String[] orderedBy = {"Name"};
        String[] orderType = {"ASC"};
        String query = t.selectAllOrdered(orderedBy, orderType);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                JsonObject json = new JsonObject();
                String name = rs.getString("Name");
                json.put("name", name);
                json.put("beschreibung", rs.getString("beschreibung"));
                json.put("max_stufe", rs.getInt("max_stufe"));
                json.put("anmerkung", rs.getString("Anmerkung"));
                json.put("dialekte", rs.getString("Dialekte"));
                json.put("schrift", rs.getString("Schrift"));
                sprachen.add(json);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sprachen;
    }

    public JsonArray getSchriftenJson() {
        JsonArray schriften = new JsonArray();
        Tabelle t = new Tabelle("\"Glossar\".\"Schriften\"");
        Connection conn = DBConnection.getConnection();
        String[] orderedBy = {"Name"};
        String[] orderType = {"ASC"};
        String query = t.selectAllOrdered(orderedBy, orderType);
        try {
            Statement stmt = conn.createStatement();

            LinkedHashMap<String, JsonObject> map = new LinkedHashMap<>();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String name = rs.getString("Name");
                JsonObject json = map.get(name);
                if (json == null) {
                    json = new JsonObject();
                    json.put("name", name);
                    json.put("steigerungskosten", rs.getInt("Steigerungskosten"));
                    json.put("alphabet", rs.getString("Alphabet"));
                }
                JsonArray sprachen = json.getJsonArray("sprachen");
                if (sprachen == null) {
                    sprachen = new JsonArray();
                }
                sprachen.add(rs.getString("Sprache"));
                json.put("sprachen", sprachen);
                map.put(name, json);
            }
            for (String key : map.keySet()) {
                schriften.add(map.get(key));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schriften;
    }

    public Quartet<JsonArray, JsonArray, JsonArray, JsonArray> getEnergieFertigkeitenJson() {
        JsonArray zauber = new JsonArray();
        JsonArray zaubertricks = new JsonArray();
        JsonArray liturigen = new JsonArray();
        JsonArray segen = new JsonArray();
        Tabelle t = new Tabelle("\"katalog\".\"energie_fertigkeiten\"");
        String[] joinSchemas = {"katalog", "katalog", "katalog"};
        String[] joinTables = {"energie_fertigkeiten_erweiterungen", "energie_fertigkeiten_verbreitungen", "energie_fertigkeiten_wirkung_qs"};
        String[] joinAttributes = {"name", "name", "name"};
        String query = t.selectAllLeftJoin("name", joinSchemas, joinTables, joinAttributes);

        String[] tables = {"energie_fertigkeiten", "energie_fertigkeiten_erweiterungen",
                "energie_fertigkeiten_erweiterungen", "energie_fertigkeiten_verbreitungen",
                "energie_fertigkeiten_wirkung_qs"};
        String[] orderedBy = {"name", "fw_voraussetzung", "ap_kosten", "voraussetzung", "qs"};
        String[] orderType = {"ASC", "ASC", "ASC", "ASC", "ASC"};
        query = t.addSort(query, tables, orderedBy, orderType);

        // System.out.println(query);
        Connection conn = DBConnection.getConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            LinkedHashMap<String, JsonObject> map = new LinkedHashMap<>();
            while(rs.next()) {
                String name = rs.getString(1);
                JsonObject json = map.get(name);
                if (json == null) {
                    json = new JsonObject();
                    json.put("name", name);
                    json.put("beschreibung", rs.getString(14));
                    json.put("wirkung", rs.getString(15));
                    json.put("kosten", rs.getString(7));
                    json.put("kosten_erhaltung", rs.getString(8));
                    json.put("attr_1", rs.getString(2));
                    json.put("attr_2", rs.getString(3));
                    json.put("attr_3", rs.getString(4));
                    json.put("mod", rs.getString(5));
                    json.put("aktions_dauer", rs.getString(6));
                    json.put("reichweite", rs.getString(9));
                    json.put("wirkungsdauer", rs.getString(10));
                    json.put("zielkategorie", rs.getString(11));
                    json.put("merkmal", rs.getString(12));
                    json.put("steigerung", rs.getString(13));
                    json.put("art", rs.getString(16));
                    // 17 energie_fertigkeiten_erweiterungen.art
                    // 18 energie_fertigkeiten_erweiterungen.name

                }

                JsonArray erweiterungen = json.getJsonArray("erweiterungen");
                if (erweiterungen == null) {
                    erweiterungen = new JsonArray();
                }
                String erweiterung = rs.getString(19);
                if (erweiterung != null && erweiterung.trim().length() > 0) {
                    JsonObject erw_json = new JsonObject();
                    erw_json.put("erweiterung", erweiterung);
                    erw_json.put("fw_voraussetzung", rs.getInt(20));
                    erw_json.put("ap_kosten", rs.getInt(21));
                    erw_json.put("beschreibung", rs.getString(22));
                    if (!erweiterungen.contains(erw_json)) {
                        erweiterungen.add(erw_json);
                    }
                }
                json.put("erweiterungen", erweiterungen);

                // 23 energie_fertigkeiten_verbreitungen.art
                // 24 energie_fertigkeiten_verbreitungen.name

                JsonArray verbr_list = json.getJsonArray("verbreitungen");
                if (verbr_list == null) {
                    verbr_list = new JsonArray();
                }
                String verbreitung = rs.getString(25);
                if (verbreitung != null && verbreitung.trim().length() > 0) {
                    if (!verbr_list.contains(verbreitung)) {
                        verbr_list.add(verbreitung);
                    }
                }
                json.put("verbreitungen", verbr_list);

                // 26 energie_fertigkeiten_wirkung_qs.name

                JsonArray wirkung_qs_list = json.getJsonArray("wirkung_qs");
                if (wirkung_qs_list == null) {
                    wirkung_qs_list = new JsonArray();
                }
                String wirkung_qs = rs.getString(28);
                if (erweiterung != null && erweiterung.trim().length() > 0) {
                    JsonObject erw_json = new JsonObject();
                    erw_json.put("wirkung", wirkung_qs);
                    erw_json.put("qs", rs.getInt(27));
                    if (!wirkung_qs_list.contains(erw_json)) {
                        wirkung_qs_list.add(erw_json);
                    }
                }
                json.put("wirkung_qs", wirkung_qs_list);
                map.put(name, json);
            }
            ArrayList<String> zauberList = new ArrayList<>(Arrays.asList("Animistenkraft", "Bannzeichen", "Elfenlied",
                    "Geodenritual", "Goblinritual", "Herrschaftsritual", "Hexenfluch", "Ritual",
                    "Schelmenstreich", "Verzerrtes Elfenlied", "Zauber", "Zaubermelodie", "Zauberrune", "Zaubertanz",
                    "Zibiljaritual"));
            ArrayList<String> geweihte = new ArrayList<>(Arrays.asList("Liturgie", "Zeremonie"));
            for (String key : map.keySet()) {
                if (map.get(key).getString("art").equals("Zaubertrick")) {
                    zaubertricks.add(map.get(key));
                } else if (map.get(key).getString("art").equals("Segen")) {
                    segen.add(map.get(key));
                } else if (zauberList.contains(map.get(key).getString("art"))) {
                    zauber.add(map.get(key));
                } else if (geweihte.contains(map.get(key).getString("art"))) {
                    liturigen.add(map.get(key));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return new Quartet<>(zauber, zaubertricks, liturigen, segen);
    }

    public Pair<JsonObject, JsonArray> getAllSfJson() {
        JsonObject sonderfertigkeiten = new JsonObject();
        JsonArray sonderfertigkeiten_all = new JsonArray();
        JsonObject unterteilungMap = new JsonObject();
        //String[] sf_type_list = {"ahnenzeichen","alhanische_zauberzeichen","allgemeine_karmale_sonderfertigkeiten","allgemeine_magische_sonderfertigkeiten","allgemeine_sonderfertigkeiten","ausbildungsaufsaetze","bannkreise_und_schutzkreise","bannschwertzauber","befehlssonderfertigkeiten","beutelzauber","chronikzauber","daemonenpakt","dolchritual","elementarpakt","erweiterte_kampfstilsonderfertigkeiten","erweiterte_liturgiestilsonderfertigkeiten","erweiterte_talentsonderfertigkeiten","erweiterte_zaubersonderfertigkeiten","feenpakt","fluggeraetritual","gewandzauber","gildenmagische_kugelzauber","gildenmagische_schalenzauber","haubenzauber","hauerkettenzauber","homunculus_sonderfertigkeiten","instrumentzauber","kampfsonderfertigkeiten","kampfstilsonderfertigkeiten","kappenzauber","karmale_traditionen","kesselzauber","keulenzauber","krallenkettenzauber","kristallomantische_kugelzauber","liturgiestilsonderfertigkeiten","lykanthropische_gaben","magische_traditionen","predigt-sonderfertigkeiten","pruegel-sonderfertigkeiten","ringzauber","schalenzauber","scharlatanische_kugelzauber","schicksalspunkte-sonderfertigkeit","schweinetrommelzauber","sichelritual","sikaryan-raub-sonderfertigkeiten","spielzeugzauber","stabzauber","steckenzauber","talentstilsonderfertigkeiten","tiersonderfertigkeiten","tricks","trinkhornzauber","vampirische_gaben","vertrautentricks","vision-sonderfertigkeiten","waffenzauber","zauberstilsonderfertigkeiten","zauberzeichen","zeremonialgegenstands-sonderfertigkeiten"};
        String[] sf_type_list = {"Ahnenzeichen","Alhanische Zauberzeichen","Allgemeine karmale Sonderfertigkeiten","Allgemeine magische Sonderfertigkeiten","Allgemeine Sonderfertigkeiten","Ausbildungsaufsätze","Bannkreise und Schutzkreise","Bannschwertzauber","Befehlssonderfertigkeiten","Beutelzauber","Chronikzauber","Dämonenpakt","Dolchritual","Elementarpakt","Erweiterte Kampfstilsonderfertigkeiten","Erweiterte Liturgiestilsonderfertigkeiten","Erweiterte Talentsonderfertigkeiten","Erweiterte Zaubersonderfertigkeiten","Feenpakt","Fluggerätritual","Gewandzauber","Gildenmagische Kugelzauber","Gildenmagische Schalenzauber","Haubenzauber","Hauerkettenzauber","Homunculus Sonderfertigkeiten","Instrumentzauber","Kampfsonderfertigkeiten","Kampfstilsonderfertigkeiten","Kappenzauber","Karmale Traditionen","Kesselzauber","Keulenzauber","Krallenkettenzauber","Kristallomantische Kugelzauber","Liturgiestilsonderfertigkeiten","Lykanthropische Gaben","Magische Traditionen","Predigt-Sonderfertigkeiten","Prügel-Sonderfertigkeiten","Ringzauber","Schalenzauber","Scharlatanische Kugelzauber","Schicksalspunkte-Sonderfertigkeit","Schweinetrommelzauber","Sichelritual","Sikaryan-Raub-Sonderfertigkeiten","Spielzeugzauber","Stabzauber","Steckenzauber","Talentstilsonderfertigkeiten","Tiersonderfertigkeiten","Tricks","Trinkhornzauber","Vampirische Gaben","Vertrautentricks","Vision-Sonderfertigkeiten","Waffenzauber","Zauberstilsonderfertigkeiten","Zauberzeichen","Zeremonialgegenstands-Sonderfertigkeiten"};
        for (String type : sf_type_list) {
            // sonderfertigkeiten.put(type, new JsonArray());
        }
        LinkedHashSet<String> uSet = new LinkedHashSet<>();
        String[] varcharValues = {"wirkung","aspekt","tradition","wohlgefaellige_talente","erweiterte_kampfsonderfertigkeiten","zeremonialgegenstand","wirkungsdauer","errata","ap-wert","merkmal","erweiterte_talentsonderfertigkeiten","leiteigenschaft","erschwernis","erweiterte_liturgiesonderfertigkeiten","kampftechnik","voraussetzungen","passender_zauberstil","volumen","kampfsftyp","regel","publikationen","verbreitung","anmerkung","wohlgefaellige_kampftechniken","traditionsartefakt","bindungskosten","tricks","tierarten","boni","asp-kosten","gebraeu","schutzkreis","bannkreis","kampftechniken","passender_liturgiestil","kreis","name","sf_art","url","spezifikation","erweiterte_zaubersonderfertigkeiten"};
        Tabelle t = new Tabelle("\"katalog\".\"sf_raw\"");
        Connection conn = DBConnection.getConnection();
        String[] orderedBy = {"unterteilung", "name", "ap-wert"};
        String[] orderType = {"ASC", "ASC", "DESC"};
        String query = t.selectAllOrdered(orderedBy, orderType);
        // System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                JsonObject sf_raw = new JsonObject();
                for (String key : varcharValues) {
                    String value = rs.getString(key);
                    if (value != null && value.trim().length() > 0) {
                        sf_raw.put(key, value);
                    }
                }
                sf_raw.put("stufen", rs.getInt("stufen"));

                String unterteilungenString = rs.getString("unterteilung");
                uSet.add(unterteilungenString);
                JsonArray unterteilungen = new JsonArray(unterteilungenString);
                sf_raw.put("unterteilung", unterteilungen);

                Queue<String> queue = new LinkedList<>();
                for (int i = 0; i < unterteilungen.size(); i++) {
                    String u = unterteilungen.getString(i);
                    queue.add(u);
                }


                sonderfertigkeiten_all.add(sf_raw);
                getMap(sonderfertigkeiten, queue, sf_raw);

                String sf_art = sf_raw.getString("sf_art");
                //sf_art = umlautReplacer(sf_art.trim()).toLowerCase();
                sf_art = sf_art.trim();

                /*
                JsonArray array = sonderfertigkeiten.getJsonArray(sf_art);
                array.add(sf_raw);
                sonderfertigkeiten.put(sf_art, array);
                */
            }
            JsonArray uArray = new JsonArray();
            for (String u : uSet) {
                JsonArray unterteilungen = new JsonArray(u);
                uArray.add(unterteilungen);
            }
            // sonderfertigkeiten.put("unterteilungen", uArray);


        } catch (SQLException e) {
            e.printStackTrace();
        }
        // System.out.println(unterteilungMap);
        return new Pair<>(sonderfertigkeiten, sonderfertigkeiten_all);
    }

    private String umlautReplacer(String s) {
        String[] umlaute = {"Ä", "Ö", "Ü", "ä", "ö", "ü", "ß", " "};
        String[] replacement = {"Ae", "Oe", "Ue", "ae", "oe", "ue", "ss", "_"};
        for (int i = 0; i < umlaute.length; i++) {
            while (s.contains(umlaute[i])) {
                s = s.replace(umlaute[i], replacement[i]);
            }
        }
        return s;
    }

    private void getMap(JsonObject json, Queue<String> queue, JsonObject sf) {
        if (queue.isEmpty()) {  // Abbruchbedingung
            JsonArray list = json.getJsonArray("list");
            if (list == null) {
                list = new JsonArray();
            }
            list.add(sf);
            // System.out.println(sf);
            json.put("list", list);
        } else {
            String key = queue.remove();
            JsonObject child = json.getJsonObject(key);
            if (child == null) {
                child = new JsonObject();
            }
            getMap(child, queue, sf);
            json.put(key, child);
        }
    }

    // name, isProfession, subArray
    public static void main(String[] args) {
        String[] list = {"Ahnenzeichen","Alhanische Zauberzeichen","Allgemeine karmale Sonderfertigkeiten","Allgemeine magische Sonderfertigkeiten","Allgemeine Sonderfertigkeiten","Ausbildungsaufsätze","Bannkreise und Schutzkreise","Bannschwertzauber","Befehlssonderfertigkeiten","Beutelzauber","Chronikzauber","Dämonenpakt","Dolchritual","Elementarpakt","Erweiterte Kampfstilsonderfertigkeiten","Erweiterte Liturgiestilsonderfertigkeiten","Erweiterte Talentsonderfertigkeiten","Erweiterte Zaubersonderfertigkeiten","Feenpakt","Fluggerätritual","Gewandzauber","Gildenmagische Kugelzauber","Gildenmagische Schalenzauber","Haubenzauber","Hauerkettenzauber","Homunculus Sonderfertigkeiten","Instrumentzauber","Kampfsonderfertigkeiten","Kampfstilsonderfertigkeiten","Kappenzauber","Karmale Traditionen","Kesselzauber","Keulenzauber","Krallenkettenzauber","Kristallomantische Kugelzauber","Liturgiestilsonderfertigkeiten","Lykanthropische Gaben","Magische Traditionen","Predigt-Sonderfertigkeiten","Prügel-Sonderfertigkeiten","Ringzauber","Schalenzauber","Scharlatanische Kugelzauber","Schicksalspunkte-Sonderfertigkeit","Schweinetrommelzauber","Sichelritual","Sikaryan-Raub-Sonderfertigkeiten","Spielzeugzauber","Stabzauber","Steckenzauber","Talentstilsonderfertigkeiten","Tiersonderfertigkeiten","Tricks","Trinkhornzauber","Vampirische Gaben","Vertrautentricks","Vision-Sonderfertigkeiten","Waffenzauber","Zauberstilsonderfertigkeiten","Zauberzeichen","Zeremonialgegenstands-Sonderfertigkeiten"};
        DBHeldenerschaffung db = DBHeldenerschaffung.singleton();
        db.addBasicAusruestung(1);
        // db.getTalenteJson();
        // System.out.println(sf);

    }

    /*
    public ArrayList<Quartet<String, String, String, String>> getRegex(String[] attribute, String[] values) {
        Tabelle t = new Tabelle("\"katalog\".\"profession\"");
        ArrayList<Quartet<String, String, String, String>> eintraege = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String query = t.selectRegex(attribute ,values);
        // System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                String name = rs.getString("name");
                String oberkategorie = rs.getString("oberkategorie");
                String unterkategorie = rs.getString("unterkategorie");
                String eintrag = rs.getString("eintrag");
                eintraege.add(new Quartet<>(name, oberkategorie, unterkategorie, eintrag));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eintraege;
    }*/

    private String escapeRegex(String rgx) {
        while (rgx.contains("[")) {
            rgx = rgx.replace("[", "{");
        }
        while (rgx.contains("]")) {
            rgx = rgx.replace("]", "}");
        }
        while (rgx.contains("{")) {
            rgx = rgx.replace("{", "\\[");
        }
        while (rgx.contains("}")) {
            rgx = rgx.replace("}", "\\]");
        }
        while (rgx.contains("(")) {
            rgx = rgx.replace("(", "{");
        }
        while (rgx.contains(")")) {
            rgx = rgx.replace(")", "}");
        }
        while (rgx.contains("{")) {
            rgx = rgx.replace("{", "\\(");
        }
        while (rgx.contains("}")) {
            rgx = rgx.replace("}", "\\)");
        }
        return rgx;
    }

    public int getUserId(String name) {
        Tabelle t = new Tabelle("\"Benutzer\".\"Benutzer\"");
        String[] wantedAttribute = {"id", "Name"};
        String[] attribute = {"Name"};
        String[] values = {name};
        int id = -1;
        Connection conn = DBConnection.getConnection();
        String query = t.selectWhereLike(wantedAttribute, attribute, values);
        System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public int addCharakter(String name, String geschlecht, String tsaTag,
                            String spezies, String kultur, String profession,
                            String haarfarbe, String augenfarbe, String schamhaare,
                            String brueste, String genital, int alter,
                            int groesse, int gewicht, String titel) {
        Tabelle t = new Tabelle("\"Charakter\".\"Charakter\"");
        int id = -1;
        boolean added = !checkIfCharakterExists(name);
        String[] attributes = {"Name", "Geschlecht", "TsaTag", "Spezies", "Kultur", "Profession",
                "Haarfarbe", "Augenfarbe", "Schamhaare", "Brueste", "Genital",
                "Alter", "Groesse", "Gewicht", "Titel"};
        Object[] values = {name, geschlecht, tsaTag, spezies, kultur, profession,
                haarfarbe, augenfarbe, schamhaare, brueste, genital,
                alter, groesse, gewicht, titel};
        int[] pk = {};

        String query = t.insertInto(attributes, values, pk);
        if (added) {
            // System.out.println(query);
            try {
                Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                while(rs.next()){
                    id = (int)rs.getFloat(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    public void addCharakterToBenutzer(int u_id, int c_id) {
        Tabelle t = new Tabelle("\"Benutzer\".\"Benutzer_Charaktere\"");
        String[] attributes = {"BenutzerID", "CharakterID"};
        Object[] values = {u_id, c_id};
        int[] pk = {0, 1};

        String query = t.insertInto(attributes, values, pk);
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkIfCharakterExists(String name) {
        boolean exists = false;
        try {
            Connection conn = DBConnection.getConnection();
            String query = "select \"CharakterID\" from \"Charakter\".\"Charakter\"\n" +
                    "    where \"Name\" = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                exists = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public void addBeinfWertForBenutzer(int id, String modifiziertes, String name, String wert, int kr) {
        Tabelle t = new Tabelle("\"Charakter\".\"Beeinflusste_Werte_"+id+"\"");
        String[] attributes = {"Modifiziertes", "Mod-Name", "Mod-Wert", "KR"};
        String mod = modifiziertes;
        if (mod == null) {
            mod = "";
        }
        Object[] values = {mod, name, wert, kr};
        int[] pk = {0, 1};
        String query = t.insertInto(attributes, values, pk);
        Connection conn = DBConnection.getConnection();
        System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addWertForBenutzer(int id, String kategorie, String name, int wert, int abzug) {
        Tabelle t = new Tabelle("\"Charakter\".\"Werte_"+id+"\"");
        String[] attributes = {"Kategorie", "Name", "Wert", "Abzug"};
        // ToDo manueller Fix
        if (name.equals("Trink-AuP")) {
            kategorie = "Zustand";
        }
        Object[] values = {kategorie, name, wert, abzug};
        int[] pk = {1};
        String query = t.insertInto(attributes, values, pk);
        Connection conn = DBConnection.getConnection();
        System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addVuNForBenutzer(int id, String name, int stufe, String kategorie, String spezifikation) {
        Tabelle t = new Tabelle("\"Charakter\".\"VuN_"+id+"\"");
        String[] attributes = {"Name", "Stufe", "Kategorie", "Spezifikation"};
        Object[] values = {name, stufe, kategorie, spezifikation};
        int[] pk = {0, 1, 2, 3};
        String query = t.insertInto(attributes, values, pk);
        Connection conn = DBConnection.getConnection();
        System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSFForBenutzer(int id, String name, int stufe, String kategorie, String spezifikation) {
        Tabelle t = new Tabelle("\"Charakter\".\"SF_"+id+"\"");
        String[] attributes = {"Name", "Stufe", "Kategorie", "Spezifikation"};
        Object[] values = {name, stufe, kategorie, spezifikation};
        int[] pk = {0, 1, 2, 3};
        String query = t.insertInto(attributes, values, pk);
        Connection conn = DBConnection.getConnection();
        System.out.println(query);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void createCharTables(int id) {
        ArrayList<String> queries = new ArrayList<>();

        queries.add("create table \"Charakter\".\"Ausruestung_" + id + "\"\n" +
                "(\n" +
                "    \"Itemid\"     integer\n" +
                "        constraint ausruestung_" + id + "_gegenstand_itemid_fk\n" +
                "            references \"Ausruestung\".\"Gegenstand\",\n" +
                "    \"Anzahl\"     integer,\n" +
                "    \"Zustand\"    double precision,\n" +
                "    \"Notiz\"      varchar,\n" +
                "    ausgeruestet boolean default true,\n" +
                "    \"Name\"       varchar,\n" +
                "    \"Trageort\"   varchar,\n" +
                "    \"CharItemId\" double precision not null\n" +
                "        constraint ausruestung_" + id + "_pk\n" +
                "            primary key,\n" +
                "    \"Mengenitem\" boolean\n" +
                ");\n" +
                "\n" +
                "alter table \"Charakter\".\"Ausruestung_" + id + "\"\n" +
                "    owner to postgres;");


        queries.add("create table \"Charakter\".\"Beeinflusste_Werte_" + id + "\"\n" +
                "(\n" +
                "    \"Modifiziertes\" varchar not null,\n" +
                "    \"Mod-Name\"      varchar not null,\n" +
                "    \"Mod-Wert\"      varchar,\n" +
                "    \"KR\"      int,\n" +
                "    constraint beeinflusste_werte_" + id + "_pk\n" +
                "        primary key (\"Modifiziertes\", \"Mod-Name\")\n" +
                ");\n" +
                "\n" +
                "alter table \"Charakter\".\"Beeinflusste_Werte_" + id + "\"\n" +
                "    owner to postgres;");

        queries.add("create table \"Charakter\".\"Werte_" + id + "\"\n" +
                "(\n" +
                "    \"Kategorie\"  varchar not null,\n" +
                "    \"Name\"  varchar not null\n" +
                "        constraint werte_" + id + "_pk\n" +
                "            primary key,\n" +
                "    \"Wert\"  integer not null,\n" +
                "    \"Abzug\" integer not null\n" +
                ");\n" +
                "\n" +
                "alter table \"Charakter\".\"Werte_" + id + "\"\n" +
                "    owner to postgres;\n" +
                "\n" +
                "create unique index werte_name_" + id + "_uindex\n" +
                "    on \"Charakter\".\"Werte_" + id + "\" (\"Name\");\n" +
                "\n");
        queries.add("create table \"Charakter\".\"VuN_" + id + "\"\n" +
                "(\n" +
                "\t\"Name\" varchar not null,\n" +
                "\t\"Stufe\" int,\n" +
                "\t\"Kategorie\" varchar,\n" +
                "\t\"Spezifikation\" varchar,\n" +
                "\tconstraint vun_" + id + "_pk\n" +
                "\t\tprimary key (\"Name\", \"Stufe\", \"Kategorie\", \"Spezifikation\")\n" +
                ");\n");


        queries.add("create table \"Charakter\".\"SF_" + id + "\"\n" +
                "(\n" +
                "\t\"Name\" varchar,\n" +
                "\t\"Stufe\" int,\n" +
                "\t\"Kategorie\" varchar,\n" +
                "\t\"Spezifikation\" varchar,\n" +
                "\tconstraint sf_id_" + id + "_pk\n" +
                "\t\tprimary key (\"Name\", \"Stufe\", \"Kategorie\", \"Spezifikation\")\n" +
                ");");
        queries.add(
                "create table \"Charakter\".\"Notiz_" + id + "\"\n" +
                        "(\n" +
                        "\t\"Name\" varchar not null\n" +
                        "\t\tconstraint notiz_" + id + "_pk\n" +
                        "\t\t\tprimary key,\n" +
                        "\t\"Inhalt\" varchar\n" +
                        ");");

        Connection conn = DBConnection.getConnection();
        for (String query : queries) {
            System.out.println(query);
            try {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();

            }
        }
        addBasicAusruestung(id);
        addBasicwerte(id);
    }

    public void addBasicAusruestung(int id) {
        Tabelle t = new Tabelle("\"Charakter\".\"Ausruestung_"+id+"\"");
        // 148,1,0,"",true,Körper,Körper,6,false
        int[] itemIds = {148,5,2,8,9,100};

        int[] anzahl = {1,0,0,0,0,1};
        int zustand = 0;
        String notiz = "";
        boolean ausgeruestet = true;

        String name[] = {"Körper","Dukat","Silbertaler","Heller","Kreuzer","Fäuste"};
        String trageort = "Körper";
        boolean[] mengenItem = {false, true, true, true, true, false};
        double[] charItemIds = new double[6];
        String idQuery = "SELECT nextval('\"Charakter\".\"CharItemId\"');";
        for (int i = 0; i < charItemIds.length; i++) {
            try {
                Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(idQuery);
                while(rs.next()){
                    charItemIds[i] = rs.getDouble(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < itemIds.length; i++) {
            String[] attributes = {
                    "Itemid",
                    "Anzahl",
                    "Zustand",
                    "Notiz",
                    "ausgeruestet",
                    "Name",
                    "Trageort",
                    "CharItemId",
                    "Mengenitem"};
            Object[] values = {itemIds[i], anzahl[i], zustand, notiz, ausgeruestet, name[i], trageort,
                    charItemIds[i], mengenItem[i]};
            int[] pk = {7};

            String query = t.insertInto(attributes, values, pk);
            try {
                Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }


    public void addBasicwerte(int id) {
        String[] arten = {"AsP","AW","Erschöpfung","GS","INI","KaP","LeP","Schip","SK","ZK"};
        String kat = "Grundwert";
        for (String s : arten) {
            addWertForBenutzer(id, kat, s, 0, 0 );
        }
        String[] arten2 = {
                "RS Kopf",
                "RS lArm",
                "RS lBein",
                "RS rArm",
                "RS rBein",
                "RS Torso"
        };
        kat = "Rüstung";
        for (String s : arten2) {
            addWertForBenutzer(id, kat, s, 0, 0 );
        }
        String[] arten3 = {
                "Baumartig",
                "Bewegungsunfähig",
                "Bewusstlos",
                "Blind",
                "Blutend",
                "Blutrausch",
                "Brennend",
                "Eingeengt",
                "Feylamia",
                "Fixiert",
                "Handlungsunfähig",
                "Hörigkeit",
                "Kind der Finsternis",
                "Kind der Nacht",
                "Krank",
                "Liegend",
                "Lykanthrop",
                "Minderer Feylamia",
                "Minderer Vampir",
                "Pechmagnet",
                "Raserei",
                "Stumm",
                "Taub",
                "Überrascht",
                "Übler Geruch",
                "Unsichtbar",
                "Vergiftet",
                "Versteinert",
                "Wergestalt"
        };
        kat = "Status";
        for (String s : arten3) {
            addWertForBenutzer(id, kat, s, 0, 0 );
        }

        String[] arten4 = {
                "Animosität",
                "Belastung",
                "Berauscht",
                "Betäubung",
                "Dämonische Auszehrung",
                "Durst",
                "Eiskalte Einflüsterung",
                "Entrückung",
                "Erregung",
                "Furcht",
                "Hunger",
                "Paralyse",
                "Schmerz",
                "Schmutz",
                "Sikaryan-Verlust",
                "Theriak-Vorrat",
                "Trance",
                "Trink-AuP",
                "Überanstrengung",
                "Verwirrung"
        };
        kat = "Zustand";
        for (String s : arten4) {
            addWertForBenutzer(id, kat, s, 0, 0 );
        }



    }
}
