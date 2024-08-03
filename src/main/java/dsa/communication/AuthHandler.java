package dsa.communication;

import dsa.db.DBBenutzer;

import java.util.ArrayList;

public class AuthHandler extends MessageHandler {
    DBBenutzer dbBenutzer = DBBenutzer.singleton();
    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        Envelope envelope = new Envelope(name, name);
        ArrayList<Envelope> envelopes = new ArrayList<>();
        System.out.println(msg);
        int id = dbBenutzer.getIdVonBenutzer(msg.body);
        System.out.println("id: " + id);
        envelope.message = new Message(msg.returnType, "-", "", 0, msg.charId, -1, id + "");
        envelopes.add(envelope);
        return envelopes;
    }
}
