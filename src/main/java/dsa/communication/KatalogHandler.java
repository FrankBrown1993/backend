package dsa.communication;

import dsa.creation.Heldenerschaffung;
import dsa.creation.HeldenerschaffungsVerwaltung;
import dsa.db.DBHeldenerschaffung;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;

import java.util.ArrayList;

public class KatalogHandler extends MessageHandler {
    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        DBHeldenerschaffung db = DBHeldenerschaffung.singleton();
        ArrayList<Envelope> envelopes = new ArrayList<>();
        System.out.println(msg.body);
        Message backmsg = new Message();
        //System.out.println(jsonArray.toString());
        backmsg.type = msg.returnType;

        if (msg.body.startsWith("katalog_")) {
            System.out.println("Message body starts with katalog_");
            String prefix = "katalog_";
            String type = msg.body.substring(prefix.length());
            System.out.println("Message body type is: " + type);
            if (type.equals("get")) {
                JsonObject json = new JsonObject();
                JsonArray vun = db.getVuNListJsonAlternativeEinzeln();
                json.put("vun", vun);
                Quintet<JsonArray, JsonArray, JsonArray, JsonArray, JsonArray> talente = db.getTalenteJson();
                json.put("talente_koerper", talente.getValue0());
                json.put("talente_gesellschaft", talente.getValue1());
                json.put("talente_natur", talente.getValue2());
                json.put("talente_wissen", talente.getValue3());
                json.put("talente_handwerk", talente.getValue4());
                Quartet<JsonArray, JsonArray, JsonArray, JsonArray> energie_fertigkeiten = db.getEnergieFertigkeitenJson();
                json.put("magie", energie_fertigkeiten.getValue0());
                json.put("zaubertricks", energie_fertigkeiten.getValue1());
                json.put("geweiht", energie_fertigkeiten.getValue2());
                json.put("segen", energie_fertigkeiten.getValue3());
                json.put("kampftechniken", db.getKampftechnikenJson());
                Pair<JsonObject, JsonArray> sf_json = db.getAllSfJson();
                json.put("sonderfertigkeiten", sf_json.getValue0());
                json.put("all_sonderfertigkeiten", sf_json.getValue1());
                json.put("sprachen", db.getSprachenJson());
                json.put("schriften", db.getSchriftenJson());

                backmsg.body = json.toString();
            }

        }
        // System.out.println(msg.body);
        Envelope envelope = new Envelope();
        envelope.reciever = name;
        envelope.message = backmsg;
        envelopes.add(envelope);
        return envelopes;
    }
}
