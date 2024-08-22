package dsa.db.converter;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpeziesConverter {

    public static JsonObject getSpeziesInfoJson(HashMap<String, String> map) {
        JsonObject json = new JsonObject();
        String[] simpleKeys = {"art", "unterart", "koerperbauundaussehen", "herkunftundverbreitung", "vermehrungundalterung", "begruessung", "verabschiedung"};
        for (String key : simpleKeys) {
            json.put(key, map.get(key));
        }
        json.put("ap", getAP(map));
        String[] vunKat = {"automatischevorteile", "dringendempfohlenevorteile", "dringendempfohlenenachteile", "typischevorteile", "typischenachteile", "untypischevorteile", "untypischenachteile"};
        for (String vun : vunKat) {
            JsonArray jsonArray = new JsonArray();
            for (Triplet<String, ArrayList<Integer>, ArrayList<String>> triplet : getVuN(map, vun)) {
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
        String[] intWerte = {"lepgrundwert", "gsgrundwert", "skgrundwert", "zkgrundwert", "erwachsenenalter", "lebenserwartung"};
        for (String string : intWerte) {
            int wert = getGrundwert(map, string);
            json.put(string, wert);
        }
        JsonArray eigModArray = new JsonArray();
        for (ArrayList<Pair<String, Integer>> eMod : getEigenschaftsMods(map)) {
            JsonArray wahlListe = new JsonArray();
            for (Pair<String, Integer> pair : eMod) {
                JsonObject eintrag = new JsonObject();
                eintrag.put("name", pair.getValue0());
                eintrag.put("wert", pair.getValue1());
                wahlListe.add(eintrag);
            }
            eigModArray.add(wahlListe);
        }
        json.put("eigenschaftsaenderungen", eigModArray);

        String[] kulturarten = {"ueblichekulturen", "restlichekulturen"};
        for (String kulturart : kulturarten) {
            System.out.println(kulturart);
            JsonArray kulturen = new JsonArray();
            for (String kultur : getKulturen(map, kulturart)) {
                if (kultur.trim().length() > 0) {
                    kulturen.add(kultur);
                }
            }
            json.put(kulturart, kulturen);
        }

        return json;
    }

    /**
     *  Contains Triplets of <b>name</b>, <b>tiers</b> and <b>specializations</b>
     */
    public static ArrayList<Triplet<String, ArrayList<Integer>, ArrayList<String>>> getVuN(HashMap<String, String> map, String art) {

        // ([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*(\s\(([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*)+(,\s([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*)+)*\))*)
        // ([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*(\s\(([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*)+(,\s([a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*)+)*\))*(\[[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+(\s[a-zA-ZäöüÄÖÜß\-&\[\]\/:0-9]+)*\])?)
        String w = "[a-zA-ZäöüÄÖÜß\\-&\\[\\]\\/:0-9’']";
        Pattern pattern = Pattern.compile("(" + w + "+(\\s" + w + "+)*(\\s\\((" + w + "+(\\s" + w + "+)*)+(,\\s(" + w + "+(\\s" + w + "+)*)+)*\\))*(\\[" + w + "+(\\s" + w + "+)*(,\\s" + w + "+(\\s" + w + "+)*)*\\])?)");
        ArrayList<Triplet<String, ArrayList<Integer>, ArrayList<String>>> list = new ArrayList<>();
        if (map != null) {
            String entry = map.get(art);
            if (entry != null) {
                Matcher matcher = pattern.matcher(entry);
                List<MatchResult> resultsList = matcher.results().collect(Collectors.toList());
                ArrayList<MatchResult> results = new ArrayList<MatchResult>(resultsList);
                for (MatchResult m : results) {
                    String eintrag = m.group();
                    Pair<String, ArrayList<Integer>> vun;
                    ArrayList<String> spezifikationen = new ArrayList<>();
                    // System.out.println("eintrag: " + eintrag);
                    if (eintrag.startsWith("Persönlichkeitsschwächen")) {
                        if (eintrag.contains("[")) {
                            String spezRaw = eintrag.substring(eintrag.indexOf("[") + 1, eintrag.trim().length() - 1);

                            // System.out.println("  spezRaw: " + spezRaw);
                            eintrag = eintrag.substring(0, eintrag.indexOf("["));
                            // System.out.println("  eintrag: " + eintrag);
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
                    } else if (eintrag.startsWith("Zusätzliche Gliedmaßen") || eintrag.startsWith("Natürliche Waffe")) {
                        vun = extractRomanLetters(eintrag);
                    } else if (eintrag.contains("(")) {
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
                            new Triplet<>(vun.getValue0(),vun.getValue1(),spezifikationen);
                    list.add(triplet);
                }
            }
        }
        return list;
    }

    public static int getGrundwert(HashMap<String, String> map, String art) {
        String entry = map.get(art);
        int wert = 0;
        if (entry != null) {
            wert = Integer.parseInt(entry);
        }
        return wert;
    }

    public static HashMap<Integer, String> getDiceThrowList(HashMap<String, String> map, String art) {
        String entry = map.get(art);
        HashMap<Integer, String> diceMap = new HashMap<>();
        if (entry != null) {
            for (String string : entry.split(Pattern.quote(", "))) {
                String[] split = string.split(Pattern.quote(": "));
                String name = split[0];
                String[] values = split[1].split(Pattern.quote(";"));
                if (values.length == 1) {
                    diceMap.put(Integer.parseInt(values[0]), name);
                } else {
                    for (int i = Integer.parseInt(values[0]); i <= Integer.parseInt(values[1]); i++) {
                        diceMap.put(i, name);
                    }
                }
            }
        }
        return diceMap;
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

    public static int getAP(HashMap<String, String> map) {
        String value = map.get("apwert");
        int ap = -1;
        if (value != null) {
            ap = Integer.parseInt(value.split(Pattern.quote(" "))[0]);
        }
        return ap;
    }

    public static String getStringValue(HashMap<String, String> map, String art) {
        return map.get(art);
    }


    public static ArrayList<ArrayList<Pair<String, Integer>>> getEigenschaftsMods(HashMap<String, String> map) {
        String entry = map.get("eigenschaftsaenderungen");
        //          list ( anz,           list ( attr, mod ) )
        ArrayList<ArrayList<Pair<String, Integer>>> list = new ArrayList<>();
        if (entry != null) {
            // CH: -1, KO: 1
            // MU: 1, KO: 1, KL: -1 || CH: -1
            // MU: 1 || KL: 1 || IN: 1 || CH: 1 || FF: 1 || GE: 1 || KO: 1 || KK: 1
            for (String eMod : entry.split(Pattern.quote(", "))) {
                ArrayList<Pair<String, Integer>> mods = new ArrayList<>();
                String[] split = eMod.split(Pattern.quote(" || "));
                for (String s : split) {
                    String[] modSplit = s.split(Pattern.quote(": "));
                    String attr = modSplit[0];
                    int mod = Integer.parseInt(modSplit[1]);
                    mods.add(new Pair<String, Integer>(attr, mod));
                }
                list.add(mods);
            }
        }
        return list;
    }

    public static ArrayList<String> getKulturen(HashMap<String, String> map, String art) {
        ArrayList<String> kulturen = new ArrayList<>();
        String entry = map.get(art);
        if (entry != null) {
            for (String kultur : entry.split(Pattern.quote(", "))) {
                kulturen.add(kultur);
            }
        }
        return kulturen;
    }
}
