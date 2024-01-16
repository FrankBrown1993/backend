package dsa.communication;

import dsa.character.Character;
import dsa.character.ModifiableValue;
import dsa.character.Profile;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CharacterHandler extends MessageHandler {
    private List<String> energies = Arrays.asList(
            "LeP", "KaP", "AsP");
    private List<String> grundwerte = Arrays.asList(
            "gs","aw","ini","sk","zk","mu","kl","ch","in","ff","ge","ko","kk");
    private List<String> zustaende = Arrays.asList(
            "erschoepfung","berauscht", "schmerz", "schmutz");
    private List<String> sozialstati = Arrays.asList("Abschaum", "Unterschicht", "Mittelstand", "Niederadel", "Adel", "Hochadel");

    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        Character character = characters.getCharacter(msg.charId);
        Envelope envelope = null;
        ArrayList<Envelope> envelopes = new ArrayList<>();
        System.out.println(msg);

        envelope = new Envelope(name, name);
        String answerBody = "";

        if (energies.contains(msg.body)) { //LeP, AsP, KaP
            JsonObject json = new JsonObject();
            String modified = msg.body;
            String modifier = msg.modifier;

            ModifiableValue mv = character.getValue(modified, modifier);
            if (mv == null) {
                mv = new ModifiableValue(
                        "Grundwert",
                        modified,
                        modifier,
                        msg.code,
                        true
                );
            } else {
                mv.modValue += msg.code;
            }
            character.addOrChangeModification(mv);

            int value = character.getModifiedByAll(modified);
            json.put("akt" + modified, value);
            answerBody = json.toString();
            envelope.message = new Message(msg.returnTo, "-", "", 0, msg.charId, -1, answerBody);
        } else if (msg.body.equals("all")) {
            JsonObject json = new JsonObject();
            /** Energien */
            for (String e : energies) {
                int maxWert = character.getGrundwert(e);
                int aktWert = character.getModifiedByAll(e);
                json.put("akt" + e, aktWert);
                json.put("max" + e, maxWert);
            }
            /** Grundwerte */
            for (String s : grundwerte) {
                int wert = character.getModifiedByAll(s);
                json.put(s, wert);
            }
            /** Zustände */
            for (String s : zustaende) {
                System.out.print(s + ": ");
                int wert = character.getModifiedByAll(s);
                System.out.println(wert);
                json.put(s, wert);
            }
            answerBody = json.toString();
            envelope.message = new Message(msg.returnTo, "-", msg.modifier, 0,
                    msg.charId, -1, answerBody);
        } else if (msg.body.equals("portrait")) {
            JsonObject json = new JsonObject();
            String portrait = character.getPortrait();
            json.put("portrait", portrait);
            answerBody = json.toString();
            envelope.message = new Message(msg.returnTo, "-", msg.modifier, 0,
                    msg.charId, -1, answerBody);
        } else if (msg.body.startsWith("data:image")) {
            character.setPortrait(msg.body);
            JsonObject json = new JsonObject();
            String portrait = character.getPortrait();
            json.put("portrait", portrait);
            answerBody = json.toString();
            envelope.message = new Message(msg.returnTo, "-", msg.modifier, 0,
                    msg.charId, -1, answerBody);
        } else if (msg.body.equals("profil")) {
            JsonObject json = new JsonObject();
            Profile profile = character.getProfile();
            json.put("name", profile.name);
            json.put("rasse", profile.spezies);
            json.put("kultur", profile.kultur);
            json.put("profession", profile.profession);
            json.put("groesse", profile.groesse);
            json.put("gewicht", profile.gewicht);
            json.put("augenfarbe", profile.augenfarbe);
            json.put("haarfarbe", profile.haarfarbe);
            json.put("geschlecht", profile.geschlecht);
            json.put("alter", profile.alter);
            int index = character.getModifiedByAll("Sozialstatus") - 1;
            json.put("sozialer_stand", sozialstati.get(index));
            json.put("titel", profile.titel);
            answerBody = json.toString();
            envelope.message = new Message(msg.returnTo, "-", msg.modifier, 0,
                    msg.charId, -1, answerBody);
        }
        envelopes.add(envelope);
        return envelopes;
    }
}
