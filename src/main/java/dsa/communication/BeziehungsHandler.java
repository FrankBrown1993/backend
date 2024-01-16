package dsa.communication;

import dsa.character.Beziehung;
import dsa.character.Character;
import dsa.db.DBCharacter;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class BeziehungsHandler extends MessageHandler{

    private DBCharacter db = DBCharacter.singleton();
    private CharacterStorage storage = CharacterStorage.singleton();
    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        Character character = characters.getCharacter(msg.charId);
        Envelope envelopeToGM = null;
        Envelope envelopeToChar = null;
        ArrayList<Envelope> envelopes = new ArrayList<>();
        System.out.println(msg);

        envelopeToGM.reciever = msg.returnTo;


        //füge Beziehung hinzu
        if(msg.code == 1) {
            String bezString = msg.body;
            String[] split = bezString.split(Pattern.quote("#"));
            Beziehung bez = new Beziehung(split[0], Integer.valueOf(split[1]), Integer.valueOf(split[2]));
            ArrayList<Beziehung> beziehungen = new ArrayList<>();
            beziehungen.add(bez);
            String erfolg = db.addBeziehungen(msg.charId, beziehungen);

            //Finde socketID
            String socketID = storage.getSocketId(msg.charId);
            envelopeToChar.reciever = socketID;

            //TO-DO: restliche message attribute?
            Message backmsg = new Message();
            JsonObject json = new JsonObject();
            json.put("name", bez.getName());
            json.put("freundschaftlich", bez.getFreundschaft());
            json.put("romantisch", bez.getRomantik());
            backmsg.body = json.toString();
            envelopeToChar.message = backmsg;
        } else if(msg.code == 2) {

        } else if(msg.code == 3) {

        } else if(msg.code == 4) {

        }

        return envelopes;
    }
}
