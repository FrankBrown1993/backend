package dsa.communication;

import dsa.character.Character;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TitelbereichHandler extends MessageHandler {
    private List<String> energies = Arrays.asList(new String[]{"LeP", "KaP", "AsP"});

    private List<String> normalValues = Arrays.asList(new String[]{
            "GS", "AW", "INI", "SK", "ZK", "MU", "KL", "CH", "IN", "GE", "FF", "KO", "KK",
            "Schmutz", "Schmerz", });

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
            int value = character.getModifiedByAll(msg.body);
            json.put("akt" + msg.body, value);
            answerBody = json.toString();

        } else if (msg.body.equals("all")) {



        }
        envelope.message = new Message(msg.returnType, "-", msg.modifier, 0, msg.charId, -1, answerBody);
        System.out.println("answer with envelope");
        envelopes.add(envelope);
        return envelopes;
    }


    /**
     * aktLeP: number;
     * maxLeP: number;
     * aktAsP: number;
     * maxAsP: number;
     * aktKaP: number;
     * maxKaP: number;
     *
     * gs: number;
     * aw: number;
     * ini: number;
     * sk: number;
     * zk: number;
     * mu: number;
     * kl: number;
     * ch: number;
     * in: number;
     * ff: number;
     * ge: number;
     * ko: number;
     * kk: number;
     *
     * schmutz: number;
     * schmerz: number;
     *
     * trunkenheit: number;
     * erschoepfung: number;
     * rausch: number;
     * ohnmacht: boolean;
     *
     * hintergrund: string;
     * datum: string;
     * bewoelkung: string;
     * aktTemp: string;
     * niederschlag: string;
     * mond: number; // 0: neu, 3: kelch, 6: voll, 9: helm
     * stunde: number;
     * minute: number;
     */
    private String getAll(Character character) {
        JsonObject json = new JsonObject();
        for (String e : energies) {
            int value = character.getModifiedByAll("e");
            json.put("aktLeP", value);
            value = character.getGrundwert("e");
            json.put("maxLeP", value);
        }


        return json.toString();
    }

}
