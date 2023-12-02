package dsa.communication;

import dsa.character.Character;
import dsa.character.ModifiableValue;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;

public class EnergyHandler extends MessageHandler {
    private List<String> energies = Arrays.asList(new String[]{"LeP", "KaP", "AsP"});

    @Override
    protected Envelope handleMessage(Message msg, String name) {
        Character character = characters.getCharacter(msg.charId);
        Envelope envelope = null;
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
            for (String e : energies) {
                int maxWert = character.getGrundwert(e);
                int aktWert = character.getModifiedByAll(e);
                json.put("akt" + e, aktWert);
                json.put("max" + e, maxWert);
            }
            answerBody = json.toString();
            envelope.message = new Message(msg.returnTo, "-", msg.modifier, 0, msg.charId, -1, answerBody);
        }


        return envelope;
    }
}
