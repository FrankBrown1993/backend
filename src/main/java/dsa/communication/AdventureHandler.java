package dsa.communication;

import dsa.character.Character;
import dsa.character.ModifiableValue;
import dsa.db.DBAbenteuer;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdventureHandler extends MessageHandler {
    DBAbenteuer dbAbenteuer = DBAbenteuer.singleton();

    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        Envelope envelope = null;
        ArrayList<Envelope> envelopes = new ArrayList<>();
        System.out.println(msg);

        envelope = new Envelope(name, name);
        String answerBody = "";

        if (msg.body.equalsIgnoreCase("all")) {
            JsonObject json = new JsonObject();
            dbAbenteuer.getAbenteuerInfos(json);
            answerBody = json.toString();
            envelope.message = new Message(msg.returnTo, "-", "", 0,
                    msg.charId, -1, answerBody);
        }
        envelopes.add(envelope);
        return envelopes;
    }
}
