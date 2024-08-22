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

public class KulturenConverter {

    public static JsonObject getKulturInfoJson(HashMap<String, String> map) {
        JsonObject json = new JsonObject();
        int ap = KulturenConverter.getAP(map);
        json.put("ap", ap);
        String[] simpleKeys = {"name", "beschreibung", "verbreitungundlebensweise", "weltsichtundglaube", "sittenundbraeuche", "trachtundbewaffnung", "ortskenntnis"};
        for (String key : simpleKeys) {
            json.put(key, map.get(key));
        }


        Pair<Integer, ArrayList<Pair<String, ArrayList<String>>>> sprachen = KulturenConverter.getSprachen(map);
        json.put("sprachen_anzahl", sprachen.getValue0());
        ArrayList<Pair<String, ArrayList<String>>> list = sprachen.getValue1();
        JsonArray sprachenArray = new JsonArray();
        for (Pair<String, ArrayList<String>> pair : list) {
            sprachenArray.add(pair.getValue0());
        }
        json.put("sprachen", sprachenArray);

        JsonArray schriftenArray = new JsonArray();
        ArrayList<Pair<String, Integer>> schriften = KulturenConverter.getSchriften(map);
        for (Pair<String, Integer> pair : schriften) {
            JsonObject schrift = new JsonObject();
            schrift.put("name", pair.getValue0());
            schrift.put("kosten", pair.getValue1());
            schriftenArray.add(schrift);
        }
        json.put("schriften", schriftenArray);

        String[] profKat = {"typischeprofession", "typischeweltlicheprofession",
                "typischezaubererprofession", "typischegeweihtenprofession"};
        for (String prof : profKat) {
            JsonArray professionenArray = new JsonArray();
            ArrayList<Triplet<String, String, String>> profList =
                    KulturenConverter.getProfessionen(map, prof);
            for (Triplet<String, String, String> p : profList) {
                JsonObject profession = new JsonObject();
                profession.put("name", p.getValue0());
                profession.put("variante", p.getValue1());
                profession.put("haeufigkeit", p.getValue2());
                professionenArray.add(profession);
            }
            json.put(prof, professionenArray);
        }

        String[] vunKat = {"typischevorteile", "typischenachteile", "untypischevorteile", "untypischenachteile"};
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
        ArrayList<String> typischeTalente = KulturenConverter.getTypischeTalente(map);
        JsonArray talenteTypischArray = new JsonArray();
        for (String t : typischeTalente) {
            talenteTypischArray.add(t);
        }
        json.put("typische_talente", talenteTypischArray);
        ArrayList<String> untypischeTalente = KulturenConverter.getUntypischeTalente(map);
        JsonArray talenteUntypischArray = new JsonArray();
        for (String t : untypischeTalente) {
            talenteUntypischArray.add(t);
        }
        json.put("untypische_talente", talenteUntypischArray);

        JsonArray talentModArray = new JsonArray();
        ArrayList<Pair<String, Integer>> talentMods = KulturenConverter.getTalentModifikatoren(map);
        for (Pair<String, Integer> tm : talentMods) {
            JsonObject talentMod = new JsonObject();
            talentMod.put("name", tm.getValue0());
            talentMod.put("wert", tm.getValue1());
            talentModArray.add(talentMod);
        }
        json.put("talent_modifikationen", talentModArray);
        return json;
    }

    /**
     * name [variante] &lt;häufigkeit&gt;
     * */
    public static ArrayList<Triplet<String, String, String>> getProfessionen(HashMap<String, String> map, String art) {
        ArrayList<Triplet<String, String, String>> list = new ArrayList<>();
        String value = map.get(art);
        if (value != null && value.length() > 0) {
            for (String v : value.split(Pattern.quote(", "))) {
                String name = v;
                String frequency = null;
                String variante = null;
                if (name.contains("[")) {
                    int index0 = name.indexOf("[");
                    int index1 = name.indexOf("]");
                    variante = name.substring(index0 + 1, index1);
                    name = name.replace(" [" + variante + "]", "").trim();
                }
                if (name.contains("<")) {
                    frequency = name.substring(name.indexOf("<") + 1, name.length() - 1);
                    name = name.substring(0, name.indexOf("<") - 1);
                }
                list.add(new Triplet<String, String, String>(name, variante, frequency));
            }
        }
        return list;
    }

    public static Pair<Integer, ArrayList<Pair<String, ArrayList<String>>>> getSprachen(HashMap<String, String> map) {
        String entry = map.get("sprache");
        ArrayList<Pair<String, ArrayList<String>>> sprachen = new ArrayList<>();
        int anz = 1;
        if (entry != null) {
            String[] split;
            if (entry.contains(", ")) { // 2 aus Garethi, Koboldisch (ohne Magieeinsatz möglich)
                split = entry.split(Pattern.quote(", "));
                anz = split.length;
            } else {  // 1 aus Alaani||Garethi||Nujuka||Thorwalsch
                split = entry.split(Pattern.quote("||"));
            }
            // Pair<String, ArrayList<String>>
            for (String str : split) {
                ArrayList<String> varList = new ArrayList<>();
                if (str.contains("(")) {
                    String varianten = str.substring(str.indexOf("(") + 1, str.length() - 1);
                    for (String v : varianten.split(Pattern.quote("; "))) {
                        varList.add(v);
                    }
                    str = str.replace(" (" + varianten + ")", "");
                }
                sprachen.add(new Pair<String, ArrayList<String>>(str, varList));
            }
        }
        return new Pair<Integer, ArrayList<Pair<String, ArrayList<String>>>>(anz, sprachen);
    }

    public static ArrayList<Pair<String, Integer>> getSchriften(HashMap<String, String> map) {
        ArrayList<Pair<String, Integer>> list = new ArrayList<>();
        String entry = map.get("schrift");
        if (entry != null && !entry.equals("keine")) {
            for (String str : entry.split(Pattern.quote(", "))) {
                String apKosten = str.substring(str.indexOf("(") + 1, str.length() - 1);
                str = str.replace(" (" + apKosten + ")", "");
                int ap = Integer.parseInt(apKosten.split(Pattern.quote(" "))[0]);
                list.add(new Pair<String, Integer>(str, ap));
            }
        }
        return list;
    }


    /**
     *  ArrayList<
     *      Pair<
     *          ArrayList<
     *              Pair<
     *                  String,
     *                  ArrayList<
     *                      Integer
     *                  >
     *              >
     *          >,
     *          ArrayList<
     *              String
     *          >
     *      >
     *  >
     *  Liste von Paaren mit namen
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
                    System.out.println("eintrag: " + eintrag);
                    if (eintrag.startsWith("Persönlichkeitsschwächen")) {
                        if (eintrag.contains("[")) {
                            String spezRaw = eintrag.substring(eintrag.indexOf("[") + 1, eintrag.trim().length() - 1);

                            System.out.println("  spezRaw: " + spezRaw);
                            eintrag = eintrag.substring(0, eintrag.indexOf("["));
                            System.out.println("  eintrag: " + eintrag);
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


    private static void convertEntry(String entry, ArrayList<Pair<ArrayList<String>, ArrayList<Integer>>> list) {
        if (entry != null) {
            String[] splitted = entry.split(Pattern.quote(", "));
            for (String kt : splitted) {
                ArrayList<String> talente = new ArrayList<>();
                ArrayList<Integer> werte = new ArrayList<>();
                if (kt.contains(":")) {
                    System.out.println(kt);
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

    public static int getAP(HashMap<String, String> map) {
        String value = map.get("paketkosten");
        int ap = -1;
        if (value != null) {
            ap = Integer.parseInt(value.split(Pattern.quote(" "))[0]);
        }
        return ap;
    }

    public static ArrayList<String> getTypischeTalente(HashMap<String, String> map) {
        ArrayList<String> list = new ArrayList<>();
        String value = map.get("typischetalente");
        if (value != null) {
            for (String v : value.split(Pattern.quote(", "))) {
                list.add(v);
            }
        }
        return list;
    }

    public static ArrayList<String> getUntypischeTalente(HashMap<String, String> map) {
        ArrayList<String> list = new ArrayList<>();
        String value = map.get("untypischetalente");
        if (value != null) {
            for (String v : value.split(Pattern.quote(", "))) {
                list.add(v);
            }
        }
        return list;
    }

    public static ArrayList<Pair<String, Integer>> getTalentModifikatoren(HashMap<String, String> map) {
        ArrayList<Pair<String, Integer>> list = new ArrayList<>();
        String value = map.get("talentmodifikatoren");
        if (value != null) {
            for (String v : value.split(Pattern.quote(", "))) {
                int i = v.lastIndexOf(" ");
                int mod = Integer.parseInt(v.substring(i + 1));
                v = v.substring(0, i);
                list.add(new Pair<>(v, mod));
            }
        }
        return list;
    }

    public static ArrayList<String> splitString(String s, String splitter) {
        return new ArrayList<>(Arrays.asList(s.split(Pattern.quote(splitter))));
    }
}
