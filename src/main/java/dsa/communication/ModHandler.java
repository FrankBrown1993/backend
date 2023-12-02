package dsa.communication;

import dsa.character.Character;
import dsa.character.ModifiableValue;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class ModHandler extends MessageHandler {
    @Override
    protected Envelope handleMessage(Message msg, String name) {
        Envelope envelope = new Envelope(name, name);
        System.out.println(msg);
        Character character = characters.getCharacter(msg.charId);
        ArrayList<ModifiableValue> list = character.getModifier(msg.body);
        JsonArray jsonArray = new JsonArray();
        for (ModifiableValue mv : list) {
            if (((mv.modValue != 0 && mv.sumMod) || (mv.modValue != 1 && !mv.sumMod)) && !mv.modifier.equalsIgnoreCase("standard")) {
                JsonObject json = new JsonObject();
                json.put("modified", mv.modified);
                json.put("modifier", mv.modifier);
                String modValue = "";
                if (!mv.sumMod) {
                    modValue = "x" + mv.modValue;
                } else {
                    if (mv.modValue > 0) {
                        modValue = "+";
                    }
                    modValue += (int)mv.modValue;
                }
                json.put("modValue", modValue);
                jsonArray.add(json);
            }
        }
        String answerBody = jsonArray.toString();
        envelope.message = new Message(msg.returnTo, "-", msg.modifier, 0, msg.charId, -1, answerBody);
        return envelope;
    }
}
