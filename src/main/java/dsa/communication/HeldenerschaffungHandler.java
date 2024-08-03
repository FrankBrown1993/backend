package dsa.communication;

import dsa.db.DBHeldenerschaffung;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class HeldenerschaffungHandler extends MessageHandler {
    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        DBHeldenerschaffung db = DBHeldenerschaffung.singleton();
        ArrayList<Envelope> envelopes = new ArrayList<>();
        System.out.println(msg.body);
        Message backmsg = new Message();
        //System.out.println(jsonArray.toString());
        backmsg.type = msg.returnType;

        if (msg.body.equals("start")) {
            ArrayList<String> erfahrungsstufen = db.getErfahrungsStufen();
            JsonArray jsonArray = new JsonArray();
            for (String s : erfahrungsstufen) {
                jsonArray.add(s);
            }
            backmsg.body = jsonArray.toString();
        }
        System.out.println(msg.body);
        // Todo
        Envelope envelope = new Envelope();
        envelope.reciever = name;
        envelope.message = backmsg;
        envelopes.add(envelope);

        return envelopes;
    }
}
