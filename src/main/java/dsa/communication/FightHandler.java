package dsa.communication;

import dsa.character.Character;
import dsa.db.DBCharacter;
import dsa.kampf.Arena;
import dsa.kampf.Fighter;
import dsa.kampf.Position;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class FightHandler extends MessageHandler {

    private DBCharacter db = DBCharacter.singleton();
    private CharacterStorage storage = CharacterStorage.singleton();

    private Arena arena = Arena.singleton();
    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        System.out.println("FightHandler handleMessage");
        Envelope envelopeToGM = new Envelope();
        //Envelope envelopeToChar = null;
        ArrayList<Envelope> envelopes = new ArrayList<>();
        System.out.println(msg);
        System.out.println("msg Header:");
        msg.printHeader();

        envelopeToGM.reciever = name;


        //bekomme alle charaktere als kämpfer aus der db
        if(msg.code == 0) {
            //TODO MOCK CHARACTERE WERDEN HNZUGEFUEGT
            storage.getCharacter(11);
            storage.getCharacter(14);
            storage.getCharacter(12);

            ArrayList<Character> characters = storage.getAllCharacters();
            Message backmsg = new Message();
            JsonArray jsonArray = new JsonArray();

            for (Character character : characters) {
                if (!arena.isCharInArena(character.getId())) {
                    JsonObject json = new JsonObject();
                    json.put("name", character.getName());
                    json.put("id", character.getId());
                    json.put("portrait", character.getPortrait());
                    json.put("token", character.getToken());
                    jsonArray.add(json);
                }
            }
            backmsg.body = jsonArray.toString();
            //System.out.println(jsonArray.toString());
            backmsg.type = msg.returnType;
            backmsg.printHeader();
            envelopeToGM.message = backmsg;

        //Fuege Fighter der Arena hinzu - Initialisierung
        } else if(msg.code == 1) {
            //Body enthält Character id und Position und flag npc ja nein
            String bodyString = msg.body;
            String[] splitted = bodyString.split(Pattern.quote("#"));
            int charId = Integer.valueOf(splitted[0]);
            Position pos = new Position(Double.valueOf(splitted[1]), Double.valueOf(splitted[2]));
            int charType = Integer.valueOf(splitted[3]);
            Character character = null;

            if (charType == 0) { //Ist ein verbundener Spieler char
                character = storage.getCharacter(charId);
            } else if (charType == 1) { //ist ein npc
                //TODO NOCH NICHT IMPLEMENTIERT
            } else if (charType == 2) { //ist ein nicht verbundener Spieler char (afk)
                character = db.loadCharacter(charId);
            }
            Fighter fighter = new Fighter(character, pos);
            arena.fighters.add(fighter);


            //send fighters back to GM
            Message backmsg = new Message();
            JsonArray jsonArray = new JsonArray();

            for (Fighter f : arena.fighters) {
                    JsonObject json = new JsonObject();
                    json.put("name", f.name);
                    json.put("id", f.id);
                    json.put("portrait", f.portrait);
                    json.put("token", f.token);
                    json.put("posX", f.position.x);
                    json.put("posY", f.position.y);
                    jsonArray.add(json);
            }
            backmsg.body = jsonArray.toString();
            backmsg.type = msg.returnType;
            //backmsg.printHeader();
            envelopeToGM.message = backmsg;
        } else if(msg.code == 2) { //send all fighters back
            //send fighters back to GM
            Message backmsg = new Message();
            JsonArray jsonArray = new JsonArray();

            for (Fighter f : arena.fighters) {
                JsonObject json = new JsonObject();
                json.put("name", f.name);
                json.put("id", f.id);
                json.put("portrait", f.portrait);
                json.put("token", f.token);
                json.put("posX", f.position.x);
                json.put("posY", f.position.y);
                jsonArray.add(json);
            }
            backmsg.body = jsonArray.toString();
            backmsg.type = msg.returnType;
            envelopeToGM.message = backmsg;
        }

        envelopes.add(envelopeToGM);
        System.out.println(envelopes);
        return envelopes;
    }
}
