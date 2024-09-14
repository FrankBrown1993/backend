package dsa.communication;

import dsa.creation.Heldenerschaffung;
import dsa.creation.HeldenerschaffungsVerwaltung;
import dsa.db.DBHeldenerschaffung;
import dsa.db.converter.ProfessionConverter;
import io.quarkus.vertx.http.runtime.devmode.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class HeldenerschaffungHandler extends MessageHandler {
    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        DBHeldenerschaffung db = DBHeldenerschaffung.singleton();
        HeldenerschaffungsVerwaltung hv = HeldenerschaffungsVerwaltung.singleton();
        ArrayList<Envelope> envelopes = new ArrayList<>();
        // System.out.println(msg.body);
        Message backmsg = new Message();
        Message apUpdate = null;
        //System.out.println(jsonArray.toString());
        backmsg.type = msg.returnType;

        /** Erschaffung */
        if (msg.body.startsWith("erschaffung_")) {
            String prefix = "erschaffung_";
            String type = msg.body.substring(prefix.length());
            if (type.equals("step")) {
                Heldenerschaffung h = hv.map.get(name);
                if (h != null) {
                    System.out.println("Heldenerschaffung präsent für " + name);
                    JsonObject json = new JsonObject();
                    json.put("step", h.step + "");
                    backmsg.body = json.toString();

                } else {
                    System.out.println("Noch keine Heldenerschaffung für " + name);
                    JsonObject json = new JsonObject();
                    json.put("step", "0");
                    backmsg.body = json.toString();
                }
            } else if (type.equals("start")) {
                Heldenerschaffung h = new Heldenerschaffung();
                h.step = 1;
                hv.map.put(name, h);
                JsonObject json = new JsonObject();
                json.put("step", h.step + "");
                backmsg.body = json.toString();
                apUpdate = createAPMessage(h);
            } else if (type.equals("weiter")) {
                Heldenerschaffung h = hv.map.get(name);
                h.step = msg.code;
                hv.map.put(name, h);
                backmsg.body = "[EMPTY]";
            } else if (type.equals("reset")) {
                hv.map.remove(name);
                JsonObject json = new JsonObject();
                json.put("step", "0");
                backmsg.body = json.toString();
            }

        /** Erfahrung */
        } else if (msg.body.startsWith("erfahrung_")) {
            String prefix = "erfahrung_";
            String type = msg.body.substring(prefix.length());
            if (type.equals("get_all")) {
                ArrayList<String> erfahrungsstufen = db.getErfahrungsStufen();
                JsonArray jsonArray = new JsonArray();
                for (String s : erfahrungsstufen) {
                    jsonArray.add(s);
                }
                backmsg.body = jsonArray.toString();
            } else if (type.equals("get_infos")) {
                Heldenerschaffung h = hv.map.get(name);
                /*
                JsonObject json = new JsonObject();
                LinkedHashMap<String, Integer> erfInfos = db.getErfahrung(h.erfahrung);
                for (String key : erfInfos.keySet()) {
                    String value = erfInfos.get(key) + "";
                    json.put(key, value);
                }
                int start_ap = erfInfos.get("ap");
                */
                JsonObject json = db.getErfahrungJson(h.erfahrung);
                int start_ap = json.getInteger("ap");
                h.start_ap = start_ap;
                hv.map.put(name, h);
                backmsg.body = json.toString();

                apUpdate = createAPMessage(h);
            } else if (type.equals("get")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = new JsonObject();
                json.put("erfahrung", h.erfahrung);
                backmsg.body = json.toString();
            } else if (type.startsWith("set_")) {
                String erfahrung = msg.body.substring(14);
                System.out.println("set Erfahrung: " + erfahrung);
                Heldenerschaffung h = hv.map.get(name);
                h.erfahrung = erfahrung;
                backmsg.body = "[EMPTY]";
                hv.map.put(name, h);
            }

        /** Spezies */
        } else if (msg.body.startsWith("spezies_")) {
            String prefix = "spezies_";
            String type = msg.body.substring(prefix.length());
            if (type.equals("get")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = new JsonObject();
                json.put("unterart", h.spezies);
                json.put("art", db.getSpeziesArt(h.spezies));
                backmsg.body = json.toString();
            } else if (type.equals("get_list")) {
                ArrayList<Pair<String, String>> list = db.getAlleSpezies();
                JsonArray jsonArray = new JsonArray();
                for (Pair<String, String> pair : list) {
                    JsonObject json = new JsonObject();
                    json.put("art", pair.getValue0());
                    json.put("unterart", pair.getValue1());
                    jsonArray.add(json);
                }
                backmsg.body = jsonArray.toString();
            } else if (type.equals("info")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = db.getSpeziesInfoJson(h.spezies);
                h.ap += json.getInteger("ap");
                hv.map.put(name, h);
                apUpdate = createAPMessage(h);
                backmsg.body = json.toString();
            } else if (type.startsWith("set_")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = db.getSpeziesInfoJson(h.spezies);
                int ap = json.getInteger("ap");
                h.ap -= ap;
                h.spezies = type.substring(4);
                hv.map.put(name, h);
            }

        /** Kultur */
        } else if (msg.body.startsWith("kultur_")) {
            String prefix = "kultur_";
            String type = msg.body.substring(prefix.length());
            if (type.equals("get")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = new JsonObject();
                json.put("name", h.kultur);
                backmsg.body = json.toString();
            } else if (type.equals("get_all")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = db.getKulturenOfSpezies(h.spezies);
                backmsg.body = json.toString();
            } else if (type.equals("info")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = db.getKulturInfoJson(h.kultur);
                h.ap += json.getInteger("ap");
                hv.map.put(name, h);
                apUpdate = createAPMessage(h);
                backmsg.body = json.toString();
            } else if (type.startsWith("set_")) {
                Heldenerschaffung h = hv.map.get(name);
                if (h.kultur != null) {
                    JsonObject json = db.getKulturInfoJson(h.kultur);
                    int ap = json.getInteger("ap");
                    h.ap -= ap;
                }
                h.kultur = type.substring(4);
                hv.map.put(name, h);
            }
        /** Profession */
        } else if (msg.body.startsWith("profession_")) {
            String prefix = "profession_";
            String type = msg.body.substring(prefix.length());
            if (type.equals("get")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = new JsonObject();
                JsonArray unterteilungList = new JsonArray();
                if (h.profession != null) {

                    JsonObject jsonProfession = db.getProfessionJson(h.profession);
                    unterteilungList = jsonProfession.getJsonArray("unterteilung");
                }
                json.put("name", unterteilungList);
                backmsg.body = json.toString();
            } else if (type.equals("get_all")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = db.getAlleProfessionenJson(msg.code, h.kultur);
                backmsg.body = json.toString();
            } else if (type.equals("info")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = db.getProfessionJson(h.profession);
                // JsonObject json = db.getProfessionInfoJson(h.profession);
                h.ap += json.getInteger("ap");
                hv.map.put(name, h);
                apUpdate = createAPMessage(h);
                backmsg.body = json.toString();
            } else if (type.startsWith("set_")) {
                String profession = type.substring(4);
                System.out.println("Try to set new profession \"" + profession + "\"");
                Heldenerschaffung h = hv.map.get(name);
                if (h.profession != null) {
                    System.out.println("Profession already present: give back the ap");
                    JsonObject json = db.getProfessionJson(h.profession);
                    int ap = json.getInteger("ap");
                    h.ap -= ap;
                } else {
                    System.out.println("no Profession present: just go ahead");
                }
                h.profession = profession;
                hv.map.put(name, h);
            }
        /** Werte */
        } else if (msg.body.startsWith("werte_")) {
            String prefix = "werte_";
            String type = msg.body.substring(prefix.length());
            System.out.println("get message with type werte_");
            if (type.equals("get")) {
                Heldenerschaffung h = hv.map.get(name);
                JsonObject json = new JsonObject();
                json.put("ap", h.ap);
                json.put("start_ap", h.start_ap);
                JsonObject erfahrungJson = db.getErfahrungJson(h.erfahrung);
                json.put("erfahrung", erfahrungJson);
                JsonObject speziesJson = db.getSpeziesInfoJson(h.spezies);
                json.put("spezies", speziesJson);
                JsonObject kulturJson = db.getKulturInfoJson(h.kultur);
                json.put("kultur", kulturJson);
                JsonObject professionJson = db.getProfessionJson(h.profession);
                json.put("profession", professionJson);
                JsonArray vun = db.getVuNListJsonAlternativeEinzeln();
                json.put("vun", vun);
                Quintet<JsonArray, JsonArray, JsonArray, JsonArray, JsonArray> talente = db.getTalenteJson();
                json.put("talente_koerper", talente.getValue0());
                json.put("talente_gesellschaft", talente.getValue1());
                json.put("talente_natur", talente.getValue2());
                json.put("talente_wissen", talente.getValue3());
                json.put("talente_handwerk", talente.getValue4());
                Quartet<JsonArray, JsonArray, JsonArray, JsonArray> energie_fertigkeiten = db.getEnergieFertigkeitenJson();
                json.put("magie", energie_fertigkeiten.getValue0());
                json.put("zaubertricks", energie_fertigkeiten.getValue1());
                json.put("geweiht", energie_fertigkeiten.getValue2());
                json.put("segen", energie_fertigkeiten.getValue3());
                json.put("kampftechniken", db.getKampftechnikenJson());
                Pair<JsonObject, JsonArray> sf_json = db.getAllSfJson();
                json.put("sonderfertigkeiten", sf_json.getValue0());
                json.put("all_sonderfertigkeiten", sf_json.getValue1());
                json.put("sprachen", db.getSprachenJson());
                json.put("schriften", db.getSchriftenJson());

                backmsg.body = json.toString();
            } else if (type.startsWith("set_")) {
                String subprefix = "set_";
                String body = type.substring(subprefix.length());
                JsonObject werte = new JsonObject(body);
                int id = Integer.parseInt(name);
                System.out.println("user id: " + id);

                Heldenerschaffung h = hv.map.get(name);
                int charId = db.addCharakter("","","", h.spezies, h.kultur, h.profession,
                        "","","","","",0,0,0,"");
                db.addCharakterToBenutzer(id, charId);
                db.createCharTables(charId);
                // AP,0#Start-AP,1200,0
                db.addBeinfWertForBenutzer(charId, "AP", "0#Start-AP", h.start_ap + "", 0);
                int ap = h.ap + werte.getInteger("ap_werte") + werte.getInteger("ap_kampftechniken");
                db.addBeinfWertForBenutzer(charId, "AP", "0#Heldenerschaffung", ap + "", 0);
                // allgemeines,Sozialstatus,2,0
                db.addWertForBenutzer(charId, "allgemeines", "Sozialstatus", 2, 0);
                JsonArray attribute = werte.getJsonArray("attributes");
                for (int i = 0; i < attribute.size(); i++) {
                    JsonObject attr = attribute.getJsonObject(i);
                    db.addWertForBenutzer(charId, "Attribut", attr.getString("name"),
                            attr.getInteger("wert"), 0);
                }
                String[] talentarten = {"talente_koerper","talente_gesellschaft",
                        "talente_natur","talente_wissen","talente_handwerk"};
                for (String art : talentarten) {
                    JsonArray talente = werte.getJsonArray(art);
                    String kategorie = "Talent";
                    for (int i = 0; i < talente.size(); i++) {
                        JsonObject t = talente.getJsonObject(i);
                        db.addWertForBenutzer(charId, kategorie, t.getString("name"),
                                t.getInteger("fw"), 0);
                    }

                }
                JsonArray kampftechniken = werte.getJsonArray("kampftechniken");
                String kategorie = "Kampftechnik";
                for (int i = 0; i < kampftechniken.size(); i++) {
                    JsonObject t = kampftechniken.getJsonObject(i);
                    db.addWertForBenutzer(charId, kategorie, t.getString("name"),
                            t.getInteger("fw"), 0);
                }

                JsonArray sonderfertigkeiten = werte.getJsonArray("sf");
                for (int i = 0; i < sonderfertigkeiten.size(); i++) {
                    JsonObject sf = sonderfertigkeiten.getJsonObject(i);
                    System.out.println(sf);
                    int stufe = 1;
                    int stf = 1;
                    try {
                        stf = sf.getInteger("stufe");
                    } catch (ClassCastException e) {
                        stf = Integer.parseInt(sf.getString("stufe"));
                    }
                    if (stf > 0) {
                        stufe = stf;
                    }
                    db.addSFForBenutzer(charId, sf.getString("name"), stufe
                            , sf.getString("kategorie"), sf.getString("spezifikation"));
                }
                JsonArray vun = werte.getJsonArray("vun");
                for (int i = 0; i < vun.size(); i++) {
                    JsonObject v = vun.getJsonObject(i);
                    int stufe = 1;
                    int stf = 1;
                    try {
                        stf = v.getInteger("stufe");
                    } catch (ClassCastException e) {
                        stf = Integer.parseInt(v.getString("stufe"));
                    }
                    if (stf > 0) {
                        stufe = stf;
                    }
                    db.addVuNForBenutzer(charId, v.getString("name"), stufe
                            , v.getString("kategorie"), v.getString("spezifikation"));
                }
                JsonArray sprachen = werte.getJsonArray("sprachen");
                for (int i = 0; i < sprachen.size(); i++) {
                    JsonObject v = sprachen.getJsonObject(i);
                    int stufe = v.getInteger("stufe");
                    if (stufe > 0) {
                        db.addSFForBenutzer(charId, v.getString("name"), stufe
                                , "", "");
                    }

                }
                JsonArray schriften = werte.getJsonArray("schriften");
                for (int i = 0; i < schriften.size(); i++) {
                    JsonObject v = schriften.getJsonObject(i);
                    int stufe = v.getInteger("stufe");
                    if (stufe > 0) {
                        db.addSFForBenutzer(charId, v.getString("name"), stufe
                                , "", "");
                    }
                }
                JsonArray zauber = werte.getJsonArray("zauber");
                kategorie = "Zauber";
                for (int i = 0; i < zauber.size(); i++) {
                    JsonObject t = zauber.getJsonObject(i);
                    db.addWertForBenutzer(charId, kategorie, t.getString("name"),
                            t.getInteger("fw"), 0);
                }

                /*
                talente_koerper: [],
                talente_gesellschaft: [],
                talente_natur: [],
                talente_wissen: [],
                talente_handwerk: [],
                            */





            }
        /** Aussehen */
        } else if (msg.body.startsWith("aussehen_")) {
            String prefix = "aussehen_";
            String type = msg.body.substring(prefix.length());
            if (type.equals("info")) {
                Heldenerschaffung h = hv.map.get(name);
                /*
                LinkedHashMap<String, String> map = db.getSpeziesInfo(h.spezies);
                JsonObject json = new JsonObject();
                for (String key : map.keySet()) {
                    json.put(key, map.get(key));
                }*/
                JsonObject json = db.getAussehenInfoJson(h.spezies);
                backmsg.body = json.toString();
            }
        }
        // System.out.println(msg.body);
        // Todo
        Envelope envelope = new Envelope();
        envelope.reciever = name;
        envelope.message = backmsg;

        envelopes.add(envelope);
        if (apUpdate != null) {
            System.out.println("add second envelope");
            Envelope envelope2 = new Envelope();
            envelope2.reciever = name;
            envelope2.message = apUpdate;
            envelopes.add(envelope2);
        }

        return envelopes;
    }

    private Message createAPMessage(Heldenerschaffung h) {
        Message apUpdate = new Message();
        apUpdate.type = "erschaffung_ap";
        JsonObject jsonAP = new JsonObject();
        jsonAP.put("ap", h.ap);
        jsonAP.put("start_ap", h.start_ap);
        apUpdate.body = jsonAP.toString();
        return apUpdate;
    }

    public static void main(String[] args) throws IOException {

        DBHeldenerschaffung db = DBHeldenerschaffung.singleton();
        String jsonString = new String(Files.readAllBytes(Paths.get("test.json")), StandardCharsets.UTF_8);
        JsonObject werte = new JsonObject(jsonString);


        int id = Integer.parseInt("9");
        System.out.println("user id: " + id);
        long time = System.currentTimeMillis();
        int charId = db.addCharakter("T" + time,"TEST","TEST", "TEST", "TEST", "TEST",
                "","","","","",0,0,0,"");
        db.addCharakterToBenutzer(id, charId);
        db.createCharTables(charId);
        // AP,0#Start-AP,1200,0
        db.addBeinfWertForBenutzer(charId, "AP", "0#Start-AP", 1200 + "", 0);
        int ap = 0 + werte.getInteger("ap_werte") + werte.getInteger("ap_kampftechniken");
        db.addBeinfWertForBenutzer(charId, "AP", "0#Heldenerschaffung", ap + "", 0);
        // allgemeines,Sozialstatus,2,0
        db.addWertForBenutzer(charId, "allgemeines", "Sozialstatus", 2, 0);
        JsonArray attribute = werte.getJsonArray("attributes");
        HashMap<String, Integer> attributeMap = new HashMap<>();
        boolean flink = false;

        for (int i = 0; i < attribute.size(); i++) {
            JsonObject attr = attribute.getJsonObject(i);
            String name = attr.getString("name");
            int wert = attr.getInteger("wert");
            attributeMap.put(name, wert);
            db.addWertForBenutzer(charId, "Attribut", name,
                    wert, 0);
        }
        String[] talentarten = {"talente_koerper","talente_gesellschaft",
                "talente_natur","talente_wissen","talente_handwerk"};
        for (String art : talentarten) {
            JsonArray talente = werte.getJsonArray(art);
            String kategorie = "Talent";
            for (int i = 0; i < talente.size(); i++) {
                JsonObject t = talente.getJsonObject(i);
                db.addWertForBenutzer(charId, kategorie, t.getString("name"),
                        t.getInteger("fw"), 0);
            }

        }
        JsonArray kampftechniken = werte.getJsonArray("kampftechniken");
        String kategorie = "Kampftechnik";
        for (int i = 0; i < kampftechniken.size(); i++) {
            JsonObject t = kampftechniken.getJsonObject(i);
            db.addWertForBenutzer(charId, kategorie, t.getString("name"),
                    t.getInteger("fw"), 0);
        }

        JsonArray sonderfertigkeiten = werte.getJsonArray("sf");
        for (int i = 0; i < sonderfertigkeiten.size(); i++) {
            JsonObject sf = sonderfertigkeiten.getJsonObject(i);
            System.out.println(sf);
            int stufe = 1;
            int stf = 1;
            try {
                stf = sf.getInteger("stufe");
            } catch (ClassCastException e) {
                stf = Integer.parseInt(sf.getString("stufe"));
            }
            if (stf > 0) {
                stufe = stf;
            }
            String name = sf.getString("name");
            if (name.equals("Flink")) {
                flink = true;
            }
            db.addSFForBenutzer(charId, name, stufe
                    , sf.getString("kategorie"), sf.getString("spezifikation"));
        }
        JsonArray vun = werte.getJsonArray("vun");
        for (int i = 0; i < vun.size(); i++) {
            JsonObject v = vun.getJsonObject(i);
            int stufe = 1;
            int stf = 1;
            try {
                stf = v.getInteger("stufe");
            } catch (ClassCastException e) {
                stf = Integer.parseInt(v.getString("stufe"));
            }
            if (stf > 0) {
                stufe = stf;
            }
            db.addVuNForBenutzer(charId, v.getString("name"), stufe
                    , v.getString("kategorie"), v.getString("spezifikation"));
        }
        JsonArray sprachen = werte.getJsonArray("sprachen");
        for (int i = 0; i < sprachen.size(); i++) {
            JsonObject v = sprachen.getJsonObject(i);
            int stufe = v.getInteger("stufe");
            if (stufe > 0) {
                db.addSFForBenutzer(charId, v.getString("name"), stufe
                        , "", "");
            }

        }
        JsonArray schriften = werte.getJsonArray("schriften");
        for (int i = 0; i < schriften.size(); i++) {
            JsonObject v = schriften.getJsonObject(i);
            int stufe = v.getInteger("stufe");
            if (stufe > 0) {
                db.addSFForBenutzer(charId, v.getString("name"), stufe
                        , "", "");
            }
        }
        JsonArray zauber = werte.getJsonArray("zauber");
        kategorie = "Zauber";
        for (int i = 0; i < zauber.size(); i++) {
            JsonObject t = zauber.getJsonObject(i);
            db.addWertForBenutzer(charId, kategorie, t.getString("name"),
                    t.getInteger("fw"), 0);
        }


        System.out.println(werte);
    }
}
