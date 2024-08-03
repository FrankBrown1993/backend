package dsa.communication;

import java.util.ArrayList;

public class CommunicationHandler extends MessageHandler {
    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        if (msg.body.equals("character enter")) {
            characters.putSocketCharacterId(name, msg.charId);
        } else if (msg.body.equals("character leave")) {
            ArrayList<String> meldungen = characters.saveCharacter(msg.charId);
            if (meldungen.size() == 0) {
                characters.removeSocketCharacter(name);
                System.out.println("character " + msg.charId + " successfully saved in db!");
                characters.removeCharacter(msg.charId);
            } else {
                System.out.println("save character on close meldungen:");
                for (String m : meldungen) {
                    System.out.println("  " + m);
                }
            }
        }
        return null;
    }
}
