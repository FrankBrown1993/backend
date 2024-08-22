package dsa.db.converter;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProfessionConverter {

    /*
    [X] "name"
    [X] "AP-Wert",""
    [X] "Voraussetzungen",""
    [X] "Sonderfertigkeiten",""
    [X] "Kampftechniken",""
    [X] "Talente","Gesellschaft"
    [X] "Talente","Handwerk"
    [X] "Talente","Körper"
    [X] "Talente","Natur"
    [X] "Talente","Wissen"
    [X] "extra AP für Talente",""
    [X] "Zauber",""
    [X] "Zauber","Zaubertricks"
    [X] "Liturgien",""
    [X] "Empfohlene Vorteile",""
    [X] "Empfohlene Nachteile",""
    [X] "Ungeeignete Vorteile",""
    [X] "Ungeeignete Nachteile",""

    [X] "Anmerkung",""
    [X] "Publikation",""
    */
    public static JsonObject getProfessionInfoJson(String professionName, HashMap<String, HashMap<String, String>> map) {
        JsonObject json = new JsonObject();

        JsonArray unterteilungList = new JsonArray();
        ArrayList<String> unterteilung = ProfessionConverter.getUnterteilung(map);
        for (String u : unterteilung) {
            unterteilungList.add(u);
        }
        json.put("unterteilung", unterteilungList);

        json.put("name", professionName);

        int ap = ProfessionConverter.getAP(map);
        json.put("ap", ap);

        json.put("beschreibung", getStringValue(map, "Beschreibung"));
        json.put("ausruestung", getStringValue(map, "Ausrüstung und Tracht"));

        JsonArray voraussArray = new JsonArray();
        for (String s : getVoraussetzungen(map)) {
            voraussArray.add(s);
        }
        json.put("voraussetzungen", voraussArray);

        ArrayList<Pair<Pair<String, ArrayList<String>>, Integer>> list = getSF(map);
        JsonArray sfList = new JsonArray();
        for (Pair<Pair<String, ArrayList<String>>, Integer> p : list) {
            JsonObject sf = new JsonObject();
            String name = p.getValue0().getValue0();
            JsonArray spez = new JsonArray();
            for (String s : p.getValue0().getValue1()) {
                spez.add(s);
            }
            int stufe = p.getValue1();
            sf.put("name", name);
            sf.put("spezialisierungen", spez);
            sf.put("stufe", stufe);
            sfList.add(sf);
        }
        json.put("sonderfertigkeiten", sfList);

        ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> ktlist = getKampftechniken(map.get("Kampftechniken"));
        JsonArray ktList = new JsonArray();
        for (Pair<ArrayList<String>, ArrayList<Integer>> pair : ktlist) {
            JsonObject kt = new JsonObject();
            ArrayList<String> talente = pair.getValue0();
            ArrayList<Integer> werte = pair.getValue1();
            JsonArray talenteList = new JsonArray();
            JsonArray werteList = new JsonArray();
            for (String t : talente) {
                talenteList.add(t);
            }
            for (Integer w : werte) {
                werteList.add(w);
            }
            kt.put("talente", talenteList);
            kt.put("werte", werteList);
            ktList.add(kt);
        }
        json.put("kampftechniken", ktList);

        HashMap<String, ArrayList<Pair<String, Integer>>> talentMap = getTalente(map.get("Talente"));
        for (String talentart : talentMap.keySet()) {
            JsonArray talentArrayJson = new JsonArray();
            for (Pair<String, Integer> pair : talentMap.get(talentart)) {
                JsonObject talentJson = new JsonObject();
                talentJson.put("name", pair.getValue0());
                talentJson.put("wert", pair.getValue1());
                talentArrayJson.add(talentJson);
            }
            String talentArtKey = umlautReplacer(talentart).toLowerCase();
            json.put("talente_" + talentArtKey, talentArrayJson);
        }

        JsonObject extraApTalente = new JsonObject();
        Triplet<Integer, Integer, ArrayList<String>> extraAp = getExtraApFuerTalente(map);
        if (extraAp != null) {
            extraApTalente.put("ap", extraAp.getValue0());
            extraApTalente.put("max_fw", extraAp.getValue1());
            JsonArray tList = new JsonArray();
            for (String t : extraAp.getValue2()) {
                tList.add(t);
            }
            extraApTalente.put("talente", tList);
        }
        json.put("extraAp", extraApTalente);


        JsonArray zList = new JsonArray();
        ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> zarray = getZauber(map.get("Zauber"));
        for (Pair<ArrayList<String>, ArrayList<Integer>> pair : zarray) {
            ArrayList<String> talente = pair.getValue0();
            ArrayList<Integer> werte = pair.getValue1();
            JsonObject obj = new JsonObject();
            JsonArray fertigkeiten = new JsonArray();
            JsonArray fw_list = new JsonArray();
            for (String t : talente) {
                JsonObject fertigkeit = new JsonObject();
                String repraesentation = "";
                if (!t.startsWith("Attributo") && !t.contains("runa)") && t.contains("(")) {
                    String[] split = t.split(Pattern.quote("("));
                    t = split[0].trim();
                    repraesentation = split[1].substring(0, split[1].length() - 1);
                }

                fertigkeit.put("name", t);
                if (repraesentation.length() > 0) {
                    fertigkeit.put("repraesentation", repraesentation);
                }
                fertigkeiten.add(fertigkeit);
            }
            for (Integer w : werte) {
                fw_list.add(w);
            }
            obj.put("fertigkeiten", fertigkeiten);
            obj.put("werte", fw_list);
            zList.add(obj);
        }
        json.put("zauber", zList);

        JsonArray ztList = new JsonArray();
        ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> ztarray = getZaubertricks(map.get("Zauber"));
        for (Pair<ArrayList<String>, ArrayList<Integer>> pair : ztarray) {
            ArrayList<String> talente = pair.getValue0();
            ArrayList<Integer> werte = pair.getValue1();
            JsonObject obj = new JsonObject();
            JsonArray fertigkeiten = new JsonArray();
            JsonArray fw_list = new JsonArray();
            for (String t : talente) {
                JsonObject fertigkeit = new JsonObject();
                String repraesentation = "";
                if (!t.startsWith("Attributo") && !t.contains("runa)") && t.contains("(")) {
                    String[] split = t.split(Pattern.quote("("));
                    t = split[0].trim();
                    repraesentation = split[1].substring(0, split[1].length() - 1);
                }

                fertigkeit.put("name", t);
                if (repraesentation.length() > 0) {
                    fertigkeit.put("repraesentation", repraesentation);
                }
                fertigkeiten.add(fertigkeit);
            }
            for (Integer w : werte) {
                fw_list.add(w);
            }
            obj.put("fertigkeiten", fertigkeiten);
            obj.put("werte", fw_list);
            ztList.add(obj);
        }
        json.put("zaubertricks", ztList);

        JsonArray litList = new JsonArray();
        ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> litarray = getLiturgien(map.get("Liturgien"));
        for (Pair<ArrayList<String>, ArrayList<Integer>> pair : litarray) {
            ArrayList<String> talente = pair.getValue0();
            ArrayList<Integer> werte = pair.getValue1();
            JsonObject obj = new JsonObject();
            JsonArray fertigkeiten = new JsonArray();
            JsonArray fw_list = new JsonArray();
            for (String t : talente) {
                JsonObject fertigkeit = new JsonObject();
                fertigkeit.put("name", t);
                fertigkeiten.add(fertigkeit);
            }
            for (Integer w : werte) {
                fw_list.add(w);
            }
            obj.put("fertigkeiten", fertigkeiten);
            obj.put("werte", fw_list);
            zList.add(obj);
        }
        json.put("liturgien", litList);

        HashMap<String, String> keyMap = new HashMap<>();
        keyMap.put("typischevorteile", "Empfohlene Vorteile");
        keyMap.put("typischenachteile", "Empfohlene Nachteile");
        keyMap.put("untypischevorteile", "Ungeeignete Vorteile");
        keyMap.put("untypischenachteile", "Ungeeignete Nachteile");

        String[] vunKat = {"typischevorteile", "typischenachteile", "untypischevorteile", "untypischenachteile"};
        for (String vun : vunKat) {
            JsonArray jsonArray = new JsonArray();
            for (Triplet<String, ArrayList<Integer>, ArrayList<String>> triplet : getVuN(map.get(keyMap.get(vun)))) {
                int index = -1;
                JsonObject jsonVuN = null;
                for (int i = 0; i < jsonArray.size(); i++) {
                    if (jsonArray.getJsonObject(i).getString("name").equals(triplet.getValue0())) {
                        JsonArray vorhanden = jsonArray.getJsonObject(i).getJsonArray("stufen");
                        boolean equal = true;
                        for (int j = 0; j < vorhanden.size(); j++) {
                            if (vorhanden.getInteger(j) != triplet.getValue1().get(j)) {
                                equal = false;
                            }
                        }
                        if (equal) {
                            jsonVuN = jsonArray.getJsonObject(i);
                            index = i;
                            break;
                        }
                    }
                }
                if (jsonVuN == null) {
                    jsonVuN = new JsonObject();
                    jsonVuN.put("name", triplet.getValue0());
                    JsonArray tiers = new JsonArray();
                    for (Integer tier : triplet.getValue1()) {
                        tiers.add(tier);
                    }
                    jsonVuN.put("stufen", tiers);
                    JsonArray specializations = new JsonArray();
                    for (String spec : triplet.getValue2()) {
                        specializations.add(spec);
                    }
                    jsonVuN.put("spezialisierungen", specializations);
                    jsonArray.add(jsonVuN);
                } else {
                    jsonArray.remove(index);
                    JsonArray specializations = jsonVuN.getJsonArray("spezialisierungen");
                    if (specializations == null) {
                        specializations = new JsonArray();
                    }
                    for (String spec : triplet.getValue2()) {
                        specializations.add(spec);
                    }
                    jsonVuN.put("spezialisierungen", specializations);
                    jsonArray.add(jsonVuN);
                }
            }
            json.put(vun, jsonArray);
        }

        json.put("publikation", getStringValue(map, "Publikation"));

        json.put("anmerkung", getStringValue(map, "Anmerkung"));

        json.put("errata", getStringValue(map, "Errata"));

        Pair<String, String> bez = getBezeichnung(map);
        json.put("name_w", bez.getValue0());
        json.put("name_m", bez.getValue1());

        return json;
    }

    /*
    public static HashMap<String, String> convertJsonToHashMap(JsonObject json) {
        HashMap<String, String> map = new HashMap<>();
        String[] stringKeys = {"name", "beschreibung", "ausruestung", ""};
        String[] arrayKeys = {"unterteilung", "voraussetzungen", ""};
        map.put("unterteilung", json.getString("unterteilung"));

        return map;
    }*/

    public static HashMap<String, String> convertJsonToHashMap(JsonObject json) {
        HashMap<String, String> map = new HashMap<>();

        Map<String, Object> jsonMap = json.getMap();
        for (String key : jsonMap.keySet()) {
            map.put(key, jsonMap.get(key).toString());
        }
        return map;
    }

    /**
     * @return Kampftechniken in auswertbarem Format. Das <b>Pair</b> enthält eine <i>ArrayList</i> mit der Fertigkeit (String) und eine
     * <i>ArrayList</i> mit Werten, die diesen zugewiesen werden dürfen (Integer).<br>
     * [Schwerter|Stangenwaffen],[12|11] bedeutet also: <br>
     * Talente Schwerter oder Stangenwaffen einmal 12 und einmal 11 nach Wahl.
     */
    public static ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> getKampftechniken(HashMap<String, String> map) {
        ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> list = new ArrayList<>();
        String[] splitted = map.get("").split(Pattern.quote(", "));
        for (String kt : splitted) {
            ArrayList<String> talente = new ArrayList<>();
            ArrayList<Integer> werte = new ArrayList<>();
            if (kt.contains(":")) {
                String[] subsplit = kt.split(":");
                String werteString = subsplit[0];
                String talenteString = subsplit[1];
                for (String t : talenteString.split(Pattern.quote("|"))) {
                    talente.add(t);
                }
                for (String w : werteString.split(Pattern.quote("|"))) {
                    werte.add(Integer.parseInt(w));
                }
            } else {
                int space = kt.lastIndexOf(" ");
                if (space != -1) {
                    talente.add(kt.substring(0, space));
                    werte.add(Integer.parseInt(kt.substring(space + 1)));
                } else {
                    talente.add(kt);
                }
            }
            Pair<ArrayList<String>, ArrayList<Integer>> pair = new Pair<ArrayList<String>, ArrayList<Integer>>(talente, werte);
            list.add(pair);
        }
        return list;
    }

    /**
     * @return Liturgien in auswertbarem Format. Das <b>Pair</b> enthält eine <i>ArrayList</i> mit der Fertigkeit (String) und eine
     * <i>ArrayList</i> mit Werten, die diesen zugewiesen werden dürfen (Integer).
     */
    public static ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> getLiturgien(HashMap<String, String> map) {
        ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> list = new ArrayList<>();
        if (map != null) {
            convertEntry(map.get(""), list);
        }
        return list;
    }


    /**
     * @return Talente als <b>Map</b> nach Talentarten in auswertbarem Format. Das <b>Pair</b> enthält eine <i>ArrayList</i> mit Talenten (String) und eine
     * <i>ArrayList</i> mit werden, die diesen zugewiesen werden dürfen (Integer).<br>
     * [Schwerter|Stangenwaffen],[12|11] bedeutet also: <br>
     * Talente Schwerter oder Stangenwaffen einmal 12 und einmal 11 nach Wahl.
     */
    public static HashMap<String, ArrayList<Pair<String, Integer>>> getTalente(HashMap<String, String> map) {
        HashMap<String, ArrayList<Pair<String, Integer>>> result = new HashMap<>();
        for (String talentart : map.keySet()) {
            ArrayList<Pair<String, Integer>> list = new ArrayList<>();
            String[] splitted = map.get(talentart).split(Pattern.quote(", "));
            for (String fert : splitted) {
                String talent = null;
                int wert = -1;
                int space = fert.lastIndexOf(" ");
                if (space != -1) {
                    talent = fert.substring(0, space);
                    wert = Integer.parseInt(fert.substring(space + 1));
                }

                Pair<String, Integer> pair = new Pair<>(talent, wert);
                list.add(pair);
            }
            result.put(talentart, list);
        }
        return result;
    }


    /**
     * (name, spezialisirung[]), stufe
     * */
    public static ArrayList<Pair<Pair<String, ArrayList<String>>, Integer>> getSF(HashMap<String, HashMap<String, String>> superMap) {
        ArrayList<Pair<Pair<String, ArrayList<String>>, Integer>> sfList = new ArrayList<>();

        Pattern pattern = Pattern.compile("([I,II,III,IV,V,VI,VII,VIII,IX,X,XI,XII,XIII]+)$");

        HashMap<String, String> map = superMap.get("Sonderfertigkeiten");
        if (map != null) {
            String entry = map.get("");
            if (entry != null) {
                String[] split = entry.split(Pattern.quote(","));
                for (String s : split) {
                    Matcher matcher = pattern.matcher(s);
                    s = s.trim();
                    int stufe = 0;
                    if (matcher.find()) {
                        String letter = matcher.group();
                        s = s.replace(" " + letter, "");
                        stufe = convertRomanLetter(matcher.group());
                    }
                    ArrayList<String> spezialisierungen = new ArrayList<>();
                    if (s.startsWith("Geländekunde")) {
                        String[] nameSpez = s.split(Pattern.quote(" ("));
                        s = nameSpez[0];
                        String spezialisierung = nameSpez[1].substring(0, nameSpez[1].length() - 1);
                        spezialisierungen = splitString(spezialisierung, "|");
                    } else if (s.startsWith("Fertigkeitsspezialisierung")) {
                        String spezialisierung = s.substring(27);
                        s = s.substring(0, 26);
                        spezialisierungen = splitString(spezialisierung, "|");
                    } else if (s.startsWith("Sprachen und Schriften für ")) {
                        String apString = s.substring(37);
                        s = "Sprachen und Schriften";
                        int ap = Integer.valueOf(apString.split(Pattern.quote(" "))[0]);
                        stufe = ap;
                    } else if (s.startsWith("Sprachen für ")) {
                        String apString = s.substring(23);
                        s = "Sprachen";
                        int ap = Integer.valueOf(apString.split(Pattern.quote(" "))[0]);
                        stufe = ap;
                    } else if (s.startsWith("Flüche für ")) {
                        String apString = s.substring(21);
                        s = "Flüche";
                        int ap = Integer.valueOf(apString.split(Pattern.quote(" "))[0]);
                        stufe = ap;
                    }
                    sfList.add(new Pair<>(new Pair<>(s, spezialisierungen), stufe));
                }
            }
        }
        return sfList;
    }

    /**
     *
     */
    public static ArrayList<Triplet<String, ArrayList<Integer>, ArrayList<String>>> getVuN(HashMap<String, String> map) {
        // ([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*(\s\(([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*)+(,\s([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*)+)*\))*)
        // ([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*(\s\(([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*)+(,\s([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*)+)*\))*(\[[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*\])?)
        String w = "[a-zA-ZäöüÄÖÜß\\-&\\[\\]\\/:0-9’']";
        // Pattern pattern = Pattern.compile("(" + w + "+(\\s" + w + "+)*(\\s\\((" + w + "+(\\s" + w + "+)*)+(,\\s(" + w + "+(\\s" + w + "+)*)+)*\\))*)");
        Pattern pattern = Pattern.compile("(" + w + "+(\\s" + w + "+)*(\\s\\((" + w + "+(\\s" + w + "+)*)+(,\\s(" + w + "+(\\s" + w + "+)*)+)*\\))*(\\[" + w + "+(\\s" + w + "+)*(,\\s" + w + "+(\\s" + w + "+)*)*\\])?)");
        ArrayList<Triplet<String, ArrayList<Integer>, ArrayList<String>>> list = new ArrayList<>();
        if (map != null) {
            String entry = map.get("");
            if (entry != null) {
                Matcher matcher = pattern.matcher(entry);
                List<MatchResult> resultsList = matcher.results().collect(Collectors.toList());
                ArrayList<MatchResult> results = new ArrayList<MatchResult>(resultsList);
                for (MatchResult m : results) {
                    String eintrag = m.group();
                    Pair<String, ArrayList<Integer>> vun;
                    String vunName;
                    ArrayList<String> spezifikationen = new ArrayList<>();
                    if (eintrag.contains("(")) {
                        String spezRaw = eintrag.substring(eintrag.indexOf("(") + 1, eintrag.trim().length() - 1);
                        eintrag = eintrag.substring(0, eintrag.indexOf("(") -1);
                        Pair<String, ArrayList<Integer>> eintragPair = extractRomanLetters(eintrag);
                        // eintrag
                        vun = eintragPair;
                        if (spezRaw.contains(",")) {
                            for (String s : spezRaw.split(",")) {
                                spezifikationen.add(s.trim());
                            }
                        } else {
                            spezifikationen.add(spezRaw);
                        }
                    } else {
                        vun = extractRomanLetters(eintrag);
                    }
                    Triplet<String, ArrayList<Integer>, ArrayList<String>> triplet =
                            new Triplet<>(vun.getValue0(), vun.getValue1(), spezifikationen);
                    list.add(triplet);
                }
            }
        }
        return list;
    }

    public static int convertRomanLetter(String l) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("I", 1);
        map.put("II", 2);
        map.put("III", 3);
        map.put("IV", 4);
        map.put("V", 5);
        map.put("VI", 6);
        map.put("VII", 7);
        map.put("VIII", 8);
        map.put("IX", 9);
        map.put("X", 10);
        map.put("XI", 11);
        map.put("XII", 12);
        map.put("XIII", 13);
        return map.get(l);
    }


    public static Pair<String, ArrayList<Integer>> extractRomanLetters(String str) {
        ArrayList<Integer> werte = new ArrayList<>();

        String[] letters = {"IX", "X","VIII", "VII", "VI", "IV", "V", "III", "II", "I"};
        int[] values = {9, 10, 8, 7, 6, 4, 5, 3, 2, 1};
        for (int i = 0; i < letters.length; i++) {
            if (str.contains(letters[i]) && str.indexOf(letters[i]) > str.length() - 4) {
                str = str.replace(letters[i], "");
                werte.add(values[i]);
            }
        }
        str = str.trim();
        if (str.endsWith("-")) {
            str = str.substring(0, str.length() - 1);
            str = str.trim();
        }
        Collections.sort(werte);
        return new Pair<>(str, werte);
    }

    public static ArrayList<String> lineToEntry(String line) {
        ArrayList<String> list = new ArrayList<>();
        if (line != null) {
            if (line.contains(")")) {
                for (String linePart: line.split(Pattern.quote(")"))) {
                    list.add(linePart);
                }
            } else {
                list.add(line);
            }
        }
        return list;
    }



    /**
     * @return Zauber in auswertbarem Format. Das <b>Pair</b> enthält eine <i>ArrayList</i> mit der Fertigkeit (String) und eine
     * <i>ArrayList</i> mit Werten, die diesen zugewiesen werden dürfen (Integer).<br>
     * [Abkühlung|Feuerfinger],[1] bedeutet also: <br>
     * Einen Zaubertrick, entweder Abkühlung oder Feuerfinger.
     */
    public static ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> getZauber(HashMap<String, String> map) {
        ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> list = new ArrayList<>();
        if (map != null) {
            convertEntry(map.get(""), list);
        }
        return list;
    }

    public static ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> getZaubertricks(HashMap<String, String> map) {
        ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> list = new ArrayList<>();
        if (map != null) {
            convertEntry(map.get("Zaubertricks"), list);
        }
        return list;
    }

    private static void convertEntry(String entry, ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> list) {
        if (entry != null) {
            String[] splitted = entry.split(Pattern.quote(", "));
            for (String kt : splitted) {
                ArrayList<String> talente = new ArrayList<>();
                ArrayList<Integer> werte = new ArrayList<>();
                if (kt.contains(":")) {
                    // System.out.println(kt);
                    String[] subsplit = kt.split(":");
                    String werteString = subsplit[0];
                    String talenteString = subsplit[1];
                    for (String t : talenteString.split(Pattern.quote("|"))) {
                        talente.add(t);
                    }
                    for (String w : werteString.split(Pattern.quote("|"))) {
                        werte.add(Integer.parseInt(w));
                    }
                } else {
                    int space = kt.lastIndexOf(" ");
                    if (space != -1) {
                        try {
                            werte.add(Integer.parseInt(kt.substring(space + 1)));
                            talente.add(kt.substring(0, space));
                        } catch (NumberFormatException e) {
                            talente.add(kt);
                        }
                    } else {
                        talente.add(kt);
                    }
                }
                Pair<ArrayList<String>, ArrayList<Integer>> pair = new Pair<ArrayList<String>, ArrayList<Integer>>(talente, werte);
                list.add(pair);
            }
        }
    }

    public static int getAP(HashMap<String, HashMap<String, String>> superMap) {
        HashMap<String, String> map = superMap.get("AP-Wert");
        int ap = -1;
        if (map != null) {
            String apString = map.get("");
            if (apString != null) {
                ap = Integer.parseInt(apString.split(Pattern.quote(" "))[0]);
            }
        }
        return ap;
    }

    public static Pair<String, String> getBezeichnung(HashMap<String, HashMap<String, String>> superMap) {
        HashMap<String, String> map = superMap.get("Bezeichnung");
        Pair<String, String> pair = null;
        if (map != null) {
            String raw = map.get("");
            if (raw != null) {
                String[] split = raw.split(Pattern.quote("|"));
                pair = new Pair<>(split[0], split[1]);
            }
        }
        return pair;
    }

    public static String getStringValue(HashMap<String, HashMap<String, String>> superMap, String type) {
        HashMap<String, String> map = superMap.get(type);
        String wanted = "";
        if (map != null) {
            String apString = map.get("");
            if (apString != null) {
                wanted = apString;
            }
        }
        return wanted;
    }

    public static ArrayList<String> getUnterteilung(HashMap<String, HashMap<String, String>> superMap) {
        HashMap<String, String> map = superMap.get("Unterteilung");
        ArrayList<String> unterteilung = new ArrayList<>();
        if (map != null) {
            String line = map.get("");
            if (line != null) {
                for (String s : line.split(Pattern.quote(">"))) {
                    unterteilung.add(s);
                }
            }
        }
        return unterteilung;
    }

    public static ArrayList<String> getVoraussetzungen(HashMap<String, HashMap<String, String>> superMap) {
        HashMap<String, String> map = superMap.get("Voraussetzungen");
        ArrayList<String> list = new ArrayList<>();
        if (map != null) {
            String line = map.get("");
            if (line != null) {
                for (String s : line.split(Pattern.quote(", "))) {
                    list.add(s);
                }
            }
        }
        return list;
    }

    /**
     * AP, maxFW, Talente[]
     * */
    public static Triplet<Integer, Integer, ArrayList<String>> getExtraApFuerTalente(HashMap<String, HashMap<String, String>> superMap) {
        HashMap<String, String> map = superMap.get("extra AP für Talente");
        Triplet<Integer, Integer, ArrayList<String>> extraAp = null;
        if (map != null) {
            String raw = map.get("");
            if (raw != null) {
                String[] splitted = raw.split(Pattern.quote(":"));
                String[] numSplit = splitted[0].split(Pattern.quote("|"));
                ArrayList<String> talente = new ArrayList<>(Arrays.asList(splitted[1].split(Pattern.quote(", "))));
                int ap = Integer.parseInt(numSplit[0]);
                int maxFw = Integer.parseInt(numSplit[1]);
                extraAp = new Triplet<>(ap, maxFw, talente);
            }
        }
        return extraAp;

    }

    public static ArrayList<String> splitString(String s, String splitter) {
        return new ArrayList<>(Arrays.asList(s.split(Pattern.quote(splitter))));
    }


    public static ArrayList<Pair<String, HashMap<String, HashMap<String, String>>>> getVarianten(HashMap<String, HashMap<String, String>> superMap, String p_name) {
        ArrayList<Pair<String, HashMap<String, HashMap<String, String>>>> variantenList = new ArrayList<>();
        HashMap<String, String> map = superMap.get("Varianten");
        if (map != null) {
            for (String key : map.keySet()) {
                HashMap<String, HashMap<String, String>> varMap = new HashMap<>();
                for (String super_key : superMap.keySet()) {
                    if (!super_key.equals("Varianten")) {
                        HashMap<String, String> subMap = new HashMap<>();
                        HashMap<String, String> superSubMap = superMap.get(super_key);
                        for (String sub_key : superSubMap.keySet()) {
                            String superEintrag = superSubMap.get(sub_key);
                            subMap.put(sub_key, superEintrag);
                        }
                        varMap.put(super_key, subMap);
                    }
                }
                updateUnterteilung(key, varMap);

                // System.out.println("  Variante: " + key);
                String value = map.get(key);
                while (value.contains(";")) {
                    value = value.replace(";", ",");
                }
                //System.out.println("  -> " + value);
                String[] split = value.split(Pattern.quote(" AP): "));
                int ap = Integer.parseInt(split[0].substring(1));
                HashMap<String, String> apMap = varMap.get("AP-Wert");
                String apEintrag = ap + " Abenteuerpunkte";
                apMap.put("", apEintrag);
                varMap.put("AP-Wert", apMap);
                //System.out.println("  kosten: " + ap);
                value = split[1];
                for (String v : value.split(", ")) {
                    if (v.startsWith("zusätzliche Voraussetzungen: ")) {
                        v = v.substring(29);
                        for (String voraus : v.split(Pattern.quote("|"))) {
                            HashMap<String, String> vMap = varMap.get("Voraussetzungen");
                            String eintrag = vMap.get("");
                            eintrag += ", " + voraus;
                            vMap.put("", eintrag);
                            varMap.put("Voraussetzungen", vMap);
                        }
                    } else {
                        int lastSpace = v.lastIndexOf(" ");
                        if (lastSpace > -1) {
                            try { // fertigkeit
                                String wertString = v.substring(lastSpace + 1);
                                int wert = Integer.parseInt(wertString);
                                processFertigkeit(v, varMap, p_name, key);

                            } catch (NumberFormatException e) {
                                if (v.contains(" statt ")) {
                                    processCommonStatt(v, varMap, p_name, key);

                                } else {
                                    addCommon(v, varMap, p_name, key);
                                }
                            }
                        } else {
                            addCommon(v, varMap, p_name, key);
                        }
                    }
                }
                variantenList.add(new Pair<>(key, varMap));
                // printProfession(varMap, key);
            }
        }
        return variantenList;
    }

    private static void updateUnterteilung(String name, HashMap<String, HashMap<String, String>> superMap) {
        HashMap<String, String> map = superMap.get("Unterteilung");
        String eintrag = map.get("");
        eintrag += ">" + name;
        map.put("", eintrag);
        superMap.put("Unterteilung", map);
    }

    private static void processFertigkeit(String value, HashMap<String, HashMap<String, String>> superMap, String p_name, String p_var) {
        if (value.endsWith(" statt 0")) {
            value = value.substring(0, value.length() - 8);
            // System.out.println("    " + value);
        }
        if (value.contains(" statt ")) {
            processFertigkeitsStatt(value, superMap, p_name, p_var);
        } else {
            addFertigkeit(value, superMap);
        }
    }

    private static void addFertigkeit(String value, HashMap<String, HashMap<String, String>> superMap) {
        int lastSpace = value.lastIndexOf(" ");
        String fwString = value.substring(lastSpace + 1);
        int fw = Integer.parseInt(fwString);
        String fertigkeit = value.substring(0, lastSpace);
        fertigkeit = correctFertigkeitName(fertigkeit);
        String fertigkeitString = fertigkeit + " " + fw;
        Pair<String, String> zuordnung = getZurodnung(fertigkeit);
        if (zuordnung != null) {
            // System.out.println("    " + fertigkeit + ": " + fw);
            // System.out.println("    " + zuordnung.getValue0() + " -> " + zuordnung.getValue1());
            HashMap<String, String> map = superMap.get(zuordnung.getValue0());
            if (map != null) {
                String eintrag = map.get(zuordnung.getValue1());
                eintrag += ", " + fertigkeitString;
                map.put(zuordnung.getValue1(), eintrag);
            } else {
                map = new HashMap<>();
                map.put(zuordnung.getValue1(), fertigkeitString);
            }
            superMap.put(zuordnung.getValue0(), map);
            // System.out.println(eintrag);
        } else {
            System.out.println("NULL " + fertigkeit);
        }
    }

    private static void addCommon(String value, HashMap<String, HashMap<String, String>> superMap, String p_name, String p_var) {
        String[] keys = {"Hauswirtschaft", "Fischer", "Gehörnte", "Waffenbau", "Rüstungsbau", "Bildhauerei", "Fertigkeitsspezialisierung", "Berittener Kampf", "Liturgien: Die Zwölf Segnungen", "Finte", "Wuchtschlag", "Zu Fall bringen", "Priester des Weins", "Empfohlener Vorteil ", "Vorstoß", "Belastungsgewöhnung"};
        String[] val0 = {"Sonderfertigkeiten", "Sonderfertigkeiten", "Sonderfertigkeiten", "Sonderfertigkeiten", "Sonderfertigkeiten", "Sonderfertigkeiten", "Sonderfertigkeiten", "Sonderfertigkeiten", "Liturgien", "Sonderfertigkeiten", "Sonderfertigkeiten", "Sonderfertigkeiten", "Sonderfertigkeiten", "Empfohlene Vorteile", "Sonderfertigkeiten", "Sonderfertigkeiten"};
        String[] val1 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
        String zuordnung0 = null;
        String zuordnung1 = null;
        for (int i = 0; i < keys.length; i++) {
            if (value.startsWith(keys[i])) {
                zuordnung0 = val0[i];
                zuordnung1 = val1[i];
            }
        }
        if (zuordnung0 != null) {
            if (value.equals("Liturgien: Die Zwölf Segnungen")) {
                value = "Geburtssegen, Tranksegen, Weisheitssegen, Harmoniesegen, Kleiner Heilsegen, Eidsegen, Glückssegen, Feuersegen, Grabsegen, Kleiner Schutzsegen, Speisesegen, Stärkungssegen";
            }
            HashMap<String, String> map = superMap.get(zuordnung0);
            if (map == null) {
                map = new HashMap<>();
                map.put(zuordnung1, value);
            } else {
                String eintrag = map.get(zuordnung1);
                eintrag += ", " + value;
                map.put(zuordnung1, eintrag);
            }
            superMap.put(zuordnung0, map);

        } else {
            if (value.startsWith("kein ")) {
                value = value.substring(5);
            }
            for (int i = 0; i < keys.length; i++) {
                if (value.startsWith(keys[i])) {
                    zuordnung0 = val0[i];
                    zuordnung1 = val1[i];
                }
            }
            if (zuordnung0 != null) {
                HashMap<String, String> map = superMap.get(zuordnung0);
                String eintrag = map.get(zuordnung1);
                if (eintrag.contains(", " + value)) {
                    eintrag = eintrag.replace(", " + value, "");
                } else if (eintrag.contains(value + ", ")) {
                    eintrag = eintrag.replace(value + ", ", "");
                } else if (eintrag.equals(value)) {
                    eintrag = eintrag.replace(value, "");
                } else {
                    System.out.println(eintrag);
                    System.out.println("FEHLER: " + p_name + " (" + p_var + "): " + value);
                }
                map.put(zuordnung1, eintrag);
                superMap.put(zuordnung0, map);
            } else {
                System.out.println("FEHLER: " + p_name + " (" + p_var + "): " + value);
            }
        }

    }

    private static  void processCommonStatt(String value, HashMap<String, HashMap<String, String>> superMap, String p_name, String p_var) {
        String[] split = value.split(Pattern.quote(" statt "));
        String pre = split[0];
        String post = split[1];
        int i = pre.lastIndexOf(" ");
        try {
            String wertStr = pre.substring(i + 1);
            String fertigkeit = pre.substring(0, i);
            int wert = Integer.parseInt(wertStr);
            Pair<String, String> zuordnung = getZurodnung(post);
            if (zuordnung != null) {
                HashMap<String, String> map = superMap.get(zuordnung.getValue0());
                // System.out.println(pre + " statt " + post);
                if (map != null) {
                    String eintrag = map.get(zuordnung.getValue1());
                    eintrag = eintrag.replace(post, pre);
                    map.put(zuordnung.getValue1(), eintrag);
                } else {
                    map = new HashMap<>();
                    map.put(zuordnung.getValue1(), pre + " " + wert);
                }
                superMap.put(zuordnung.getValue0(), map);
            } else {
                System.out.println("NULL " + value);

            }

        } catch (NumberFormatException e) {
            if (post.startsWith("Fertigkeitsspezialisierung")) {
                HashMap<String, String> map = superMap.get("Sonderfertigkeiten");
                String eintrag = map.get("");
                eintrag = eintrag.replace(post, pre);
                map.put("", eintrag);
                superMap.put("Sonderfertigkeiten", map);
            } else {
                System.out.println("BEARBEITEN: " + value);
            }
        }

    }

    private static void processFertigkeitsStatt(String value, HashMap<String, HashMap<String, String>> superMap, String p_name, String p_var) {
        // System.out.println(value);
        String stattRaw = value;
        int index = value.lastIndexOf(" ");
        int fwVorhanden = Integer.parseInt(value.substring(index + 1));
        value = value.substring(0, index);
        index = value.lastIndexOf(" ");
        value = value.substring(0, index);
        index = value.lastIndexOf(" ");
        int fw = Integer.parseInt(value.substring(index + 1));
        value = value.substring(0, index);
        value = correctFertigkeitName(value);
        Pair<String, String> zuordnung = getZurodnung(value);
        if (zuordnung != null) {
            String searchString = value + " " + fwVorhanden;
            String replaceWith = value + " " + fw;
            HashMap<String, String> map = superMap.get(zuordnung.getValue0());
            if (map != null) {
                String eintrag = map.get(zuordnung.getValue1());

                if (eintrag.contains(value) && !eintrag.contains(searchString)) {
                    if (zuordnung.getValue0().equals("Kampftechniken") && fwVorhanden == 6) {
                        addFertigkeit(replaceWith, superMap);
                    } else {
                        // System.out.println(p_name + " (" + p_var + ")");
                        // System.out.println("  " + stattRaw);
                    }
                } else {
                    eintrag = eintrag.replace(searchString, replaceWith);
                    map.put(zuordnung.getValue1(), eintrag);
                }
            } else {
                map = new HashMap<>();
                map.put(zuordnung.getValue1(), replaceWith);
            }
            superMap.put(zuordnung.getValue0(), map);
        } else {
            System.out.println("zuordnung null! " + value);
        }
    }

    private static String correctFertigkeitName(String fertigkeit) {
        if (fertigkeit.equals("Alchimie")) {
            fertigkeit = "Alchemie";
        } else if (fertigkeit.equals("Lebensmittelverarbeitung")) {
            fertigkeit = "Lebensmittelbearbeitung";
        } else if (fertigkeit.equals("Heilkunde Krankheit")) {
            fertigkeit = "Heilkunde Krankheiten";
        }
        return fertigkeit;
    }

    static ArrayList<String> talenteK = new ArrayList<>(Arrays.asList("Fliegen", "Gaukeleien", "Klettern", "Körperbeherrschung", "Kraftakt", "Reiten", "Schwimmen", "Selbstbeherrschung", "Singen", "Sinnesschärfe", "Tanzen", "Taschendiebstahl", "Verbergen", "Zechen"));
    static ArrayList<String> talenteN = new ArrayList<>(Arrays.asList("Fährtensuchen", "Fesseln", "Fischen & Angeln", "Orientierung", "Pflanzenkunde", "Tierkunde", "Wildnisleben"));
    static ArrayList<String> talenteH = new ArrayList<>(Arrays.asList("Alchemie", "Boote & Schiffe", "Fahrzeuge", "Handel", "Heilkunde Gift", "Heilkunde Krankheiten", "Heilkunde Seele", "Heilkunde Wunden", "Holzbearbeitung", "Lebensmittelbearbeitung", "Lederbearbeitung", "Malen & Zeichnen", "Metallbearbeitung", "Musizieren", "Schlösserknacken", "Steinbearbeitung", "Stoffbearbeitung"));
    static ArrayList<String> talenteW = new ArrayList<>(Arrays.asList("Brett- & Glücksspiel", "Geographie", "Geschichtswissen", "Götter & Kulte", "Kriegskunst", "Magiekunde", "Mechanik", "Rechnen", "Rechtskunde", "Sagen & Legenden", "Sphärenkunde", "Sternkunde"));
    static ArrayList<String> talenteG = new ArrayList<>(Arrays.asList("Bekehren & Überzeugen", "Betören", "Einschüchtern", "Etikette", "Gassenwissen", "Menschenkenntnis", "Überreden", "Verkleiden", "Willenskraft"));

    static ArrayList<String> kampffertigkeiten = new ArrayList<>(Arrays.asList("Armbrüste", "Blasrohre", "Bögen", "Diskusse", "Dolche", "Fächer", "Fechtwaffen", "Feuerspeien", "Hiebwaffen", "Kettenwaffen", "Lanzen", "Peitschen", "Raufen", "Schilde", "Schleudern", "Schwerter", "Spießwaffen", "Stangenwaffen", "Wurfwaffen", "Zweihandhiebwaffen", "Zweihandschwerter"));
    static ArrayList<String> zauber = new ArrayList<>(Arrays.asList("Blut trinken", "Brazoraghs Hieb", "Durch feste Materie", "Großer Sprung", "Harte Haut", "Immunität gegen Hitze", "Immunität gegen Kälte", "Kampffähigkeiten verbessern", "Kraft aus Schmerzen", "Mächtiger Patronruf (Adler) I", "Mächtiger Patronruf (Adler) II", "Mächtiger Patronruf (Bär) I", "Mächtiger Patronruf (Bär) II", "Mächtiger Patronruf (Eule) I", "Mächtiger Patronruf (Eule) II", "Mächtiger Patronruf (Falke) I", "Mächtiger Patronruf (Falke) II", "Mächtiger Patronruf (Feuermolch) I", "Mächtiger Patronruf (Feuermolch) II", "Mächtiger Patronruf (Fischotter) I", "Mächtiger Patronruf (Fischotter) II", "Mächtiger Patronruf (Gepard) I", "Mächtiger Patronruf (Gepard) II", "Mächtiger Patronruf (Jaguar) I", "Mächtiger Patronruf (Jaguar) II", "Mächtiger Patronruf (Khoramsbestie) I", "Mächtiger Patronruf (Khoramsbestie) II", "Mächtiger Patronruf (Khoramswühler) I", "Mächtiger Patronruf (Khoramswühler) II", "Mächtiger Patronruf (Löwe) I", "Mächtiger Patronruf (Löwe) II", "Mächtiger Patronruf (Luchs) I", "Mächtiger Patronruf (Luchs) II", "Mächtiger Patronruf (Mammut) I", "Mächtiger Patronruf (Mammut) II", "Mächtiger Patronruf (Mungo) I", "Mächtiger Patronruf (Mungo) II", "Mächtiger Patronruf (Nashorn) I", "Mächtiger Patronruf (Nashorn) II", "Mächtiger Patronruf (Nebelkrähe) I", "Mächtiger Patronruf (Nebelkrähe) II", "Mächtiger Patronruf (Pferd) I", "Mächtiger Patronruf (Pferd) II", "Mächtiger Patronruf (Rabe) I", "Mächtiger Patronruf (Rabe) II", "Mächtiger Patronruf (Säbelzahntiger) I", "Mächtiger Patronruf (Säbelzahntiger) II", "Mächtiger Patronruf (Sandwolf) I", "Mächtiger Patronruf (Sandwolf) II", "Mächtiger Patronruf (Stier) I", "Mächtiger Patronruf (Stier) II", "Mächtiger Patronruf (Vielfraß) I", "Mächtiger Patronruf (Vielfraß) II", "Mächtiger Patronruf (Widder) I", "Mächtiger Patronruf (Widder) II", "Mächtiger Patronruf (Wildkatze) I", "Mächtiger Patronruf (Wildkatze) II", "Mächtiger Patronruf (Wildschwein) I", "Mächtiger Patronruf (Wildschwein) II", "Mächtiger Patronruf (Wolf) I", "Mächtiger Patronruf (Wolf) II", "Mächtiger Patronruf (Würgeschlange) I", "Mächtiger Patronruf (Würgeschlange) II", "Patronruf", "Schneller Angriff", "Steinmacht", "Talentverbesserung", "Tiergeistsprache", "Tierkräfte I", "Tierkräfte II", "Tierkräfte III", "Tierverwandlung (Adler)", "Tierverwandlung (Bär)", "Tierverwandlung (Eule)", "Tierverwandlung (Falke)", "Tierverwandlung (Feuermolch)", "Tierverwandlung (Fischotter)", "Tierverwandlung (Gebirgsbock)", "Tierverwandlung (Gepard)", "Tierverwandlung (Jaguar)", "Tierverwandlung (Khoramsbestie)", "Tierverwandlung (Khoramswühler)", "Tierverwandlung (Löwe)", "Tierverwandlung (Luchs)", "Tierverwandlung (Mammut)", "Tierverwandlung (Mungo)", "Tierverwandlung (Nashorn)", "Tierverwandlung (Nebelkrähe)", "Tierverwandlung (Pferd)", "Tierverwandlung (Rabe)", "Tierverwandlung (Säbelzahntiger)", "Tierverwandlung (Sandwolf)", "Tierverwandlung (Stier)", "Tierverwandlung (Vielfraß)", "Tierverwandlung (Widder)", "Tierverwandlung (Wildkatze)", "Tierverwandlung (Wildschwein)", "Tierverwandlung (Wolf)", "Tierverwandlung (Würgeschlange)", "Trümmerschlag", "Unbeschadeter Sturz", "Unerschöpflich", "Bannzeichen der Geisterurne", "Bannzeichen des Dämonengefäßes", "Bannzeichen wider (Daimonide & Chimären)", "Bannzeichen wider (Dämonen)", "Bannzeichen wider (Feen)", "Bannzeichen wider (Geister)", "Bannzeichen wider Gift", "Bannzeichen wider Schnee und Eis", "Bannzeichen wider (Spinnen)", "Bannzeichen wider Stürme", "Bannzeichen wider (Untote)", "Bannzeichen wider Zauberei", "Bannzeichen wider Zerstörung", "Siegel der Seelenruhe", "Erinnerungsmelodie", "Freundschaftslied", "Friedenslied", "Lied der Abwehr dämonischer Mächte", "Lied der Erholung", "Lied der Klarheit", "Lied der Lieder", "Lied der Pflanzen", "Lied der Reinheit", "Lied der Tierwahrnehmung", "Lied des Heilschlafs", "Lied des Trostes", "Lied des Windgeflüsters", "Lied des Zauberschutzes", "Melodie der Kunstfertigkeit", "Sorgenlied", "Zaubermelodie", "Gestalt aus Rauch", "Trank des ungehinderten Weges", "Aufmerksamer Wächter", "Elementarer Verbündeter", "Goblin-Bannzone", "Goblin-Exorzismus", "Goblin-Geisterfalle", "Goblin-Geistheilung", "Goblin-Hauch", "Goblin-Kriegsmatsch", "Goblin-Pflanzenwuchs", "Goblin-Regentanz", "Goblin-Tabuzone", "Goblin-Zuflucht", "Orvai Kurims Kriegstrommel", "Wildschweineruf", "Ausbruch unterdrückter Gefühle", "Figur der Schmerzen", "Fluch der Pestilenz", "Fluch der Schlaflosigkeit", "Kristall der Herrschaft", "Macht über Schlafwandler", "Wurzel des Blutes", "Ängste mehren", "Beiß auf Granit!", "Beute!", "Fellwechsel", "Geschmackssinn nehmen", "Gestank anheften", "Hagelschlag", "Hexenschuss", "Hunger wecken", "Juckreiz verursachen", "Kornfäule", "Krötenkuss", "Miese Laune", "Mit Blindheit schlagen", "Pech an den Hals wünschen", "Pestilenz", "Schlaf rauben", "Todesfluch", "Unfruchtbarkeit", "Viehverstümmelung", "Warzen sprießen", "Wollust verursachen", "Zunge lähmen", "Zwei linke Hände", "Abneigungen und Vorlieben erzeugen", "Accuratum", "Adamantium", "Animatio", "Applicatus", "Arcanovi", "Band der Freundschaft", "Bärenruhe", "Begierde auslösen", "Blick durch fremde Augen", "Blick in die Vergangenheit", "Brandform", "Caldofrigo", "Chimaeroform", "Chronoklassis", "Chrononautos", "Custodosigil", "Dämonenpakt beenden", "Destructibo", "Dschinnenruf", "Eins mit der Natur", "Elementarer Diener", "Ergebenheit der Wogen", "Erhabenheit des Marmors", "Felsenform", "Freiheit der Wolken", "Gefäß der Jahre", "Geisterbeschwörung", "Geisterruf", "Gletscherform", "Hagelschlag und Sturmgebrüll", "Hartes schmelze", "Immortalis Lebenszeit", "Infinitum Immerdar", "Invocatio Maior", "Invocatio Maxima", "Invocatio Minor", "Klarheit des Eises", "Körperlose Reise", "Lawinenfall und Trümmerfeld", "Leidensbund", "Madas Spiegel", "Magischer Raub", "Meister der Elemente", "Memorabia Falsifir", "Movimento", "Nekropathia", "Nihilogravo", "Pflanzenform", "Planastrale", "Reinheit der Lohe", "Ruf der Feenwesen", "Ruhe Körper", "Seelenwanderung", "Standhafter Wächter", "Stein wandle", "Totes handle", "Transmutare", "Traumgestalt", "Überlegener Krieger", "Übertragung der Liebeskünste", "Unberührt von Satinav", "Weiches erstarre", "Weisheit der Bäume", "Widerwille", "Wirbelform", "Wogenform", "Xenographus", "Zauberklinge Geisterspeer", "Zaubernahrung", "Zauberschnurren", "Zauberwesen der Natur", "Zauberzwang", "Aufgeblasen", "Dichter und Denker", "Holterdipolter", "Juckpulver", "Koboldgeschenk", "Lach dich gesund", "Lachkrampf", "Langer Lulatsch - kleiner Dotz", "Meister minderer Geister", "Murks und Patz", "Nackedei", "Papperlapapp", "Schelmenkleister", "Schelmenlaune", "Schelmenmaske", "Schelmenrausch", "Tauschrausch", "Verschwindibus", "Zagibu Ubigaz", "Kakophonie des Wahnsinns", "Lied des Schmerzes", "Schlachtlied", "Sklavenlied", "Sturmlied", "Ablativum", "Abvenenum", "Adlerauge", "Adlerschwinge", "Aeolito", "Affenarme", "Affenruf", "Alpgestalt", "Altisonus", "Analys Arkanstruktur", "Angst auslösen", "Ängste lindern", "Aquafaxius", "Aquaqueris", "Aquasphaero", "Arachnea", "Archofaxius", "Archosphaero", "Armatrutz", "Aromatis Illusionis", "Atemnot", "Attributo (Charisma)", "Attributo (Fingerfertigkeit)", "Attributo (Gewandtheit)", "Attributo (Intuition)", "Attributo (Klugheit)", "Attributo (Konstitution)", "Attributo (Körperkraft)", "Attributo (Mut)", "Aufwecken", "Auge des Limbus", "Aura der Erschöpfung", "Aureolus", "Auris Illusionis", "Avilea", "Axxeleratus", "Balsam Salabunde", "Band und Fessel", "Bannbaladin", "Basaltleib", "Begehren erzeugen", "Blick aufs Wesen", "Blick in die Gedanken", "Blindheit", "Blitzball", "Blitz dich find", "Böser Blick", "Brandungsleib", "Brennender Hass", "Brustformung", "Chamaelioni", "Claudibus", "Corpofesso", "Corpofrigo", "Dämonenbann", "Dämonenschild", "Dämonisches Vergessen", "Debilitatio", "Desintegratus", "Disruptivo", "Dornenwand", "Dunkelheit", "Duplicatus", "Ecliptifactus", "Eichenleib", "Eigene Ängste", "Eigene Dummheit", "Einflussbann", "Eisenrost", "Eispfeil", "Eiswand", "Elementarbann", "Elfenstimme", "Erinnerung verlasse dich", "Erregung spüren", "Erschöpfungen lindern", "Erzpfeil", "Eulenruf", "Exposami", "Falkenauge", "Favilludo", "Federleib", "Feenstaub", "Fesselfeld", "Feuchte Erregung", "Feuerpfeil", "Firnlauf", "Fischflosse", "Flammenwand", "Fledermausruf", "Flim Flam", "Foramen", "Fortifex", "Frigifaxius", "Frigisphaero", "Frostleib", "Fulminictus", "Gardianum", "Gedankenbilder", "Gefunden", "Geisteressenz", "Gifthaut", "Glutlauf", "Große Gier", "Große Verwirrung", "Halluzination", "Harmlose Gestalt", "Haselbusch", "Heilungsbann", "Hellsichtbann", "Heptagramma", "Herr über das Tierreich", "Herzschlag ruhe", "Hexagramma", "Hexengalle", "Hexenholz", "Hexenknoten", "Hexenkrallen", "Hexenspeichel", "Hilfreiche Pfote", "Hilfreiche Schwinge", "Hilfreiche Tatze", "Himmelslauf", "Höllenpein", "Hornissenruf", "Horriphobus", "Humofaxius", "Humosphaero", "Humuspfeil", "Ignifaxius", "Ignisphaero", "Ignorantia", "Illusionsbann", "Imperavi", "Impersona", "Incendio", "Invercano", "Invinculo", "Invocatio Minima", "Karnifilo", "Katzenaugen", "Katzenruf", "Klarum Purum", "Krabbelnder Schrecken", "Kraft des Tieres", "Krähenruf", "Krötensprung", "Kulminatio", "Kusch", "Last des Alters", "Levthans Feuer", "Luftpfeil", "Lunge des Leviatan", "Mal der Erschöpfung", "Mal der Schwäche", "Manifesto", "Manus Illusionis", "Manus Miracula", "Memorans", "Menetekel", "Motoricus", "Nebelform", "Nebelwand", "Nuntiovolo", "Objectobscuro", "Objectofixo", "Objectovoco", "Objektbann", "Oculus Astralis", "Oculus Illusionis", "Odem Arcanum", "Oktagramma", "Orcanofaxius", "Orcanosphaero", "Pandaemonium", "Panik überkomme euch", "Paralysis", "Penetrizzel", "Penisformung", "Pentagramma", "Pestilenz erspüren", "Pestodem", "Physiostabilis", "Plumbumbarum", "Projectimago", "Protectionis", "Psychostabilis", "Radau", "Reflectimago", "Regeneratio", "Reptilea", "Respondami", "Salander", "Sanfter Fall", "Sanftmut", "Sapefacta", "Satuarias Herrlichkeit", "Schimmernder Schild", "Schlangenruf", "Schlechte Ausstrahlung", "Schleier der Unwissenheit", "Schmerzen lindern", "Schuppenhaut", "Schwarzer Schrecken", "Schwarz und Rot", "Seelentier erkennen", "Seidenzunge", "Sensattacco", "Sensibar", "Serpentialis", "Silentium", "Sinesigil", "Skelettarius", "Solidirid", "Somnigravis", "Sphärenbann", "Spielzeug der Lust", "Spinnenlauf", "Spinnenruf", "Spurlos", "Standfest", "Steinwand", "Stillstand", "Sturm der Verunsicherung", "Sturmwand", "Sumus Elixiere", "Taubheit", "Telekinesebann", "Temporalbann", "Tempus Stasis", "Tiere besprechen", "Tiergedanken", "Transversalis", "Unentflammbarkeit", "Ungeschickt", "Vaginaformung", "Verlangen kontrollieren", "Verunsicherung", "Verwandlungsbann", "Vipernblick", "Visibili", "Vogelzwitschern", "Wasseratem", "Wasserpfeil", "Welle der Reinigung", "Welle des Schmerzes", "Wellenlauf", "Wellenwand", "Wipfellauf", "Woge der Versteinerung", "Wolfstatze", "Wüstenlauf", "Zauberpferd herbeirufen", "Zitterfinger", "Zorn der Elemente", "Zunge betäuben", "Zweifel schüren", "Zwingtanz", "Melodie der Angriffslust", "Melodie der Beleidigungen", "Melodie der Erlösung", "Melodie der Ermutigung", "Melodie der Feen", "Melodie der Flammen", "Melodie der Freundschaft", "Melodie der Geschwindigkeit", "Melodie der Heilung", "Melodie der Motivation", "Melodie der Täuschung", "Melodie der Tiere", "Melodie der Versöhnung", "Melodie der Verwirrung", "Melodie der Weisheit", "Melodie der Wüste", "Melodie der Zähigkeit", "Melodie des Bebens", "Melodie des Einlullens", "Melodie des Handels", "Melodie des Kampfes", "Melodie des Magieschadens", "Melodie des Meeres", "Melodie des Rausches", "Melodie des Verbergens", "Melodie des Windes", "Melodie des Zauberschutzes", "Melodie des Zögerns", "Entgiftungsrune (Eidurbanruna)", "Felsenrune (Björgruna)", "Feuerschutzrune (Eldurvernruna)", "Finsterrune (Warteruna)", "Friedensrune (Fjöterlundruna)", "Furchtrune (Vargruna)", "Lebensrune (Livruna)", "Nebelbannrune (Thokebanruna)", "Orkanstimmenrune (Hringjavindruna)", "Ottarune (Ottaruna)", "Pfeilrune (Boltruna)", "Rauschrune (Hugibaniruna)", "Rüstrune (Verndunruna)", "Salzwasserrune (Sjövannruna)", "Schicksalsrune (Wyrdruna)", "Schutzrune vor Alfen (Alfibanruna)", "Schutzrune vor Daimoniden & Chimären (Skepnabanruna)", "Schutzrune vor Dämonen (Vondurbanruna)", "Schutzrune vor Elementaren (Verabanruna)", "Schutzrune vor Geistern (Vandrendabanruna)", "Schutzrune vor Hranngargezücht (Fylgjaruna)", "Schutzrune vor Stürmen (Vagakoruna)", "Schutzrune vor Untote (Draugerbanruna)", "Schutzrune vor Zauberei (Galderbanruna)", "Stärkerune (Styrkurruna)", "Waffenrune (Aescruna)", "Tanz der Angriffslust", "Tanz der Beweglichkeit", "Tanz der Bilder", "Tanz der Erlösung", "Tanz der Ermutigung", "Tanz der Heilung", "Tanz der Liebe", "Tanz der Linderung", "Tanz der magischen Gemeinschaft", "Tanz der Täuschung", "Tanz der Träume", "Tanz der Unantastbarkeit", "Tanz der Unverletzbarkeit", "Tanz der Verteidigung", "Tanz der Verwirrung", "Tanz der Wacht", "Tanz der Wahrheit", "Tanz der Weisheit", "Tanz des Begehrens", "Tanz des Blutrausches", "Tanz des Feuers", "Tanz des Handels", "Tanz des Jagdglücks", "Tanz des Jagdpechs", "Tanz des Ungehorsams", "Tanz ohne Ende", "Band zur Ware", "Bienenfleiss", "Bienenkönigin", "Bienensuche", "Bienentanz", "Sicheres Lager", "Siegel des Grabes", "Traumerinnerung", "Traumseherin", "Unglücksgefäss", "Wachshaut", "Wachskonservierung", "Wachsmumie", "Warenchronik", "Weisheit der Schrift", "Drachenleib", "Krötenruf", "Sumpfstrudel", "Cryptographo", "Kraft des Humus", "Warmes gefriere", "Papageienruf", "Eiseskälte Kämpferherz", "Aerofugo", "Granit und Marmor"));
    static ArrayList<String> zauberTricks = new ArrayList<>(Arrays.asList("Abkühlung", "Aranische Rasur", "Astrales Leuchten", "Auffälliger Stil", "Aura der Eitelkeit", "Ausziehen von Zauberhand", "Bartwuchs", "Bauchreden", "Beeren & Nüsse", "Bereit zum Aufbruch", "Beruhigung", "Blick in den Limbus", "Blütenduft", "Dämonling", "Duft", "Ehrfürchtiges Verhalten", "Einfache Telekinese", "Eiskalter Blick", "Elementarling", "Elfenhaar", "Erotischer Traum", "Essenz der Niederhöllen", "Feenfüße", "Feenzauber", "Feuerfinger", "Feuerspiel", "Flammenhaar", "Geistergeräusche", "Geräuschhexerei", "Glücksgriff", "Grußworte", "Guten Morgen", "Haarpracht", "Hand der Antimagie", "Handwärmer", "Heilsame Berührung", "Heller Stern", "Hexenblick", "Hintergrundmusik", "Hundekeks", "Insektenbann", "Kehrbesen", "Kleiner Fliegender Teppich", "Kleingeld", "Kneifen", "Konstante Temperatur", "Körperpflege", "Kriegsbemalung", "Laufendes Händchen", "Leselampe", "Leuchtblume", "Lockruf", "Luftstoß", "Lüsterne Hand", "Nagellack", "Ölhaut", "Ordentlich", "Pflanzenempathie", "Putziges Tierchen", "Regenbogenaugen", "Rohrstock", "Ruhige Ausstrahlung", "Rührlöffel", "Rutschiger Boden", "Sandfigur", "Schamhaarfrisur", "Schattenspiel", "Schlangenhände", "Schminken", "Schmutzabweisend", "Schnipsen", "Selbstarchivierung", "Siegel der Elemente", "Signatur", "Sprechendes Kästchen", "Steinempathie", "Strahlendes Lächeln", "Tätowierung", "Tentakelgriff", "Tierpflege", "Tiertrick", "Totenmaske", "Trocken", "Türklopfer", "Übergangen", "Unauffälliger Stab", "Unheimliches Lachen", "Verbundenheitsgefühl", "Verneigung", "Verrücktes Kichern", "Waffensäuberung", "Warmes Blut", "Wasserhöhe", "Wechselschein", "Weisheit des Leytfadens", "Wind im Haar", "Wohlgeruch", "Würze", "Zauberfeder"));
    static ArrayList<String> liturgien = new ArrayList<>(Arrays.asList("Angriffslust", "Angriffswelle", "Auge des Jägers", "Ausnüchtern", "Bann der Dunkelheit", "Bann der Furcht", "Bann der göttlichen Gaben", "Bann des Lichts", "Bannstrahl", "Bann wider Untote", "Bannzone", "Bärenhaut", "Befehl des Schamanen", "Befreiung des Geistes", "Begnadeter Reiter", "Berauschen", "Besänftigung", "Bescheidenheit", "Beschwörung der gemeinen Diener des Rattenkindes", "Blendstrahl", "Blendung", "Blick auf den Meeresgrund", "Blick des Heilers", "Blitzschlag", "Blutiger Zorn", "Blutiges Siegel", "Blutzoll", "Bodenweihe", "Botschaft aus der Tiefe", "Brazoraghs Krieger", "Büchersuche", "Dämonenwall", "Delphinruf", "Des Einen bezaubernder Sphärenklang", "Doppelgänger", "Ehrenhaftigkeit", "Ehrlicher Vertrag", "Entfesselung", "Entstelltes Antlitz", "Entzifferung", "Erdbeben", "Ermutigung", "Ernüchterung", "Erregender Rausch", "Ertrinken", "Erwachen", "Erzene Opfergabe", "Fall ins Nichts", "Fesselndes Band", "Feuerwall", "Flugechsenruf", "Freundschaftliches Auftreten", "Friedfertigkeit", "Friedvolle Aura", "Friedvoller Rausch", "Froststurm", "Furchteinflößende Tiergeister", "Furchtresistenz", "Gebieter der Flammen", "Gefühlskälte", "Geisterblick", "Geisterfalle", "Geldwechsel", "Gesegneter Rausch", "Getreidewachstum", "Giftbann", "Gnadenstoß", "Goldene Hand", "Goldene Rüstung", "Göttliche Klinge", "Göttlicher Fingerzeig", "Göttlicher Rausch", "Göttliches Zeichen", "Göttliche Verständigung", "Hairuf", "Hauch des Elements", "Heiliger Befehl", "Heiliges Liebesspiel", "Heilsame Quelle", "Heilsegen", "Heldenkraft", "Helfende Hand", "Herbeirufung der Heerscharen des Rattenkindes (Ratten)", "Herbeirufung der Heerscharen des Rattenkindes (Schakale)", "Herbeirufung der Heerscharen des Rattenkindes (Vampirfledermäuse)", "Herbeirufung der Heerscharen des Rattenkindes (Wolfsspinnen)", "Herbeirufung von Tairachs Dienern (Nebelkrähen)", "Herr der Flammen", "Hilfreiche Seele", "Innere Ruhe", "Jaguarruf", "Kälteexplosion", "Kampfgeschick", "Klarer Geist", "Kleidungschamäleon", "Kleiner Bannstrahl", "Kleiner Bann wider Untote", "Kleine Windhose", "Kraftvoller Körper", "Krankheiten vorbeugen", "Krankheitsbann", "Kriegsfarben", "Lautlos", "Lebensschutz", "Levthanischer Liebhaber", "Liturgieschild", "Lust erzeugen", "Macht des Levthan", "Mächtiger Angriff", "Magieanalyse", "Magiebann", "Magieschutz", "Magiesicht", "Magiespiegel", "Mammutruf", "Maske", "Mauereinsturz", "Meeresungeheuer vertreiben", "Mit Dummheit schlagen", "Mondsicht", "Mondsilberzunge", "Motivation", "Namenlose Kälte", "Namenlose Raserei", "Namenloses Vergessen", "Namenlose Zweifel", "Nebelkrähenschwarm", "Numinorus Fesseln", "Obarans Blendung", "Objektsegen", "Obsession", "Offenlegung des Geistes", "Ogerruf", "Opfergang", "Ort der Ruhe", "Pech und Schwefel", "Pech-und-Schwefel-Strahl", "Pflanzenwuchs", "Polarbärenruf", "Quallenhaut", "Quallenruf", "Rabenruf", "Rattenschwarm", "Regenbogenbrücke", "Reinigung des Bösen", "Rinderruf", "Ruf der Heimat", "Schattenfessel", "Schiffsgespür", "Schlaf", "Schlangenruf", "Schlangenstab", "Schlangenzunge", "Schleichende Fäulnis", "Schleichende Fäulnis (Pflanzen)", "Schlingerruf", "Schmerzresistenz", "Schonfrist", "Schutz der Wehrlosen", "Schutzsegen", "Schwindende Zauberkraft", "Seelenschatten", "Seevogelsprache", "Sicherer Weg", "Sicht in der Dunkelheit", "Sprache des Tapams", "Steinhaut", "Sternenglanz", "Sturmruf", "Talismanruf", "Tierbeherrschung", "Tiere beruhigen", "Tierleid lindern", "Tiersprache", "Trankfluch", "Treuer Begleiter", "Unsichtbare Flut", "Unterwasseratmung", "Untotenerhebung", "Vampirische Kräfte", "Verstecktes Begehren", "Versteinerung", "Wahrheit", "Wand wider Dämonen", "Wasserlauf", "Weihe des Bodens", "Wieselflink", "Windhose", "Windruf", "Wolfsruf", "Wundersame Verständigung", "Zähe Haut", "Zwergenmacht", "Eidsegen", "Feuersegen", "Geburtssegen", "Glückssegen", "Grabsegen", "Harmoniesegen", "Kleiner Heilsegen", "Kleiner Schutzsegen", "Speisesegen", "Stärkungssegen", "Tranksegen", "Weisheitssegen", "Ächtung (Exkommunikation)", "Ackersegen", "Arcanum Interdictum", "Aufnahme (Initiation)", "Ausbrennen (Purgation)", "Bannfluch (Anathema)", "Berauschender Wein", "Beschwörung der hohen Diener des Rattenkindes", "Beschwörung der machtvollen Diener des Rattenkindes", "Bild für die Ewigkeit", "Blick in die Flammen", "Dämonenpakt brechen", "Das Löschen des Lichts", "Delphingestalt", "Diener der Erde", "Diener der Flammen", "Diener der Kälte", "Diener der Wellen", "Diener der Wolken", "Diener des Erzes", "Eidechsengestalt", "Eidechsenregeneration", "Einflüsterung", "Eingeschworene Mannschaft", "Eisbärgestalt", "Elsterngestalt", "Empfängnis des Korsmals", "Erfolgreiche Pflanzensuche", "Erinnern", "Erschaffung von Nachtkindern", "Exorzismus", "Falkengestalt", "Fest der Freude", "Flugechsengestalt", "Freie Seelenfahrt", "Frostschutz", "Fruchtbarkeit", "Fuchsgestalt", "Gänsegestalt", "Geistersprache", "Geistheilung", "Geschlechterwechsel", "Geschwinder Schritt", "Gespräch mit den Toten", "Geweihter Panzer", "Gnade des Vergessens", "Göttliche Erkenntnis", "Göttliche Präzision", "Greifenruf", "Grosse Waffenweihe", "Guter Fang", "Häutung", "Heiliger Schwur", "Heilschlaf", "Heilung von Seelenkranken", "Herr der Meere", "Hilfe in der Not", "Himmlische Schatzkammer", "Hundegestalt", "Inspiration", "Jagdglück", "Jaguargestalt", "Jugendlichkeit", "Kleine Moralstärkung", "Kriegszustand", "Läuterung des Erzes", "Lebenstausch", "Leichtfüssig", "Leitende Strömung", "Liebestätowierung", "Liturgieabsorption", "Löwengestalt", "Luchsgestalt", "Machtvoller Exorzismus", "Makelloser Leib", "Marbidenmacht", "Metallerhitzung", "Moralstärkung", "Mungogestalt", "Nahrungsreinigung", "Nebelleib", "Nebelschwaden", "Numinorus Fluch", "Objektweihe", "Ogerbindung", "Ortsweihe (Sanctum)", "Panthergestalt", "Paradiesvogelgestalt", "Pferdegestalt", "Pflanzenkraft", "Priesterweihe (Ordination)", "Rabengestalt", "Rat der Ahnen", "Regenkontrolle", "Reisesegen", "Sättigung", "Schaffenskraft", "Schattenrochengestalt", "Schlangenfluch", "Schlangengestalt", "Schmetterlingsgestalt", "Schwanengestalt", "Seelenbannung", "Seelenprüfung", "Seemonsterruf", "Segnung des Heims", "Selbstopferung", "Sicherer Tritt", "Sippenbann", "Speisung", "Stärkung der Wildnis", "Staub und Schimmel", "Storchengestalt", "Tabu-Zone", "Tairachs machtvolle Erhebung von Untoten", "Talismanverankerung", "Taubengestalt", "Tempelweihe (Konsekration)", "Traumbild", "Traumgesicht", "Unbeschwerte Wanderung", "Untote erschaffen", "Verblassende Erinnerung", "Vergessen", "Waffenfluch", "Wegweiser", "Weihe des Heims", "Widdergestalt", "Wiederherstellung", "Winterschlaf", "Wolfsfluch", "Wolfsgestalt", "Zuflucht", "Zwergische Verbrüderung", "Zwergwalgestalt", "Empfängnisverhütung", "Erregung kontrollieren", "Gespiegelte Gefühle", "Gigantische Geilheit", "Hand der Lust", "Hastiger Höhepunkt", "Krankheitsvorbeugung"));

    private static Pair<String, String> getZurodnung(String s) {
        Pair<String, String> pair = null;
        if (talenteK.contains(s)) {
            pair = new Pair<String, String>("Talente", "Körper");
        } else if (talenteN.contains(s)) {
            pair = new Pair<String, String>("Talente", "Natur");
        } else if (talenteH.contains(s)) {
            pair = new Pair<String, String>("Talente", "Handwerk");
        } else if (talenteW.contains(s)) {
            pair = new Pair<String, String>("Talente", "Wissen");
        } else if (talenteG.contains(s)) {
            pair = new Pair<String, String>("Talente", "Gesellschaft");
        } else if (kampffertigkeiten.contains(s)) {
            pair = new Pair<String, String>("Kampftechniken", "");
        } else if (zauber.contains(s)) {
            pair = new Pair<String, String>("Zauber", "");
        } else if (zauberTricks.contains(s)) {
            pair = new Pair<String, String>("Zauber", "Zaubertricks");
        } else if (liturgien.contains(s)) {
            pair = new Pair<String, String>("Liturgien", "");
        }
        return pair;
    }

    public static void printProfession(String name, HashMap<String, HashMap<String, String>> superMap) {
        System.out.println(name);
        for (String super_key : superMap.keySet()) {
            System.out.println("  " + super_key);
            HashMap<String, String> subMap = superMap.get(super_key);
            for (String sub_key : subMap.keySet()) {
                if (sub_key.length() > 0) {
                    System.out.println("    " + sub_key);
                }
                System.out.println("      " + subMap.get(sub_key));
            }
        }
        System.out.println();

    }

    private static String umlautReplacer(String s) {
        String[] umlaute = {"Ä", "Ö", "Ü", "ä", "ö", "ü", "ß", " "};
        String[] replacement = {"Ae", "Oe", "Ue", "ae", "oe", "ue", "ss", "_"};
        for (int i = 0; i < umlaute.length; i++) {
            while (s.contains(umlaute[i])) {
                s = s.replace(umlaute[i], replacement[i]);
            }
        }
        return s;
    }
}
