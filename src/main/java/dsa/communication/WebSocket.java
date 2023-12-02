package dsa.communication;

import dsa.character.Character;
import dsa.db.BackupService;
import dsa.db.DBCharacter;
import io.quarkus.vertx.http.runtime.devmode.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObjectBuilder;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.validation.constraints.Negative;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import static java.util.Objects.requireNonNull;

@ServerEndpoint("/ws/{name}")
@ApplicationScoped
public class WebSocket {

    private Map<String, Session> socketSessions = new HashMap<>();

    private Map<String, Message[]> messageSequence = new HashMap<>();
    private Map<String, Boolean> finishedSequence = new HashMap<>();

    boolean timerRunning = false;

    private DBCharacter dbCharacter = DBCharacter.singleton();
    private CharacterStorage characters = CharacterStorage.singleton();

    public Session getSessionOfUser(String user) {
        return socketSessions.get(user);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name) {
        socketSessions.put(name, session);
        System.out.println("onOpen> " + name);

        /*if (!timerRunning) {
            timerRunning = true;
            int seconds = 900;
            new Timer().schedule(new BackupService(), 0, 1000 * seconds);
        }*/

        // ToDo: line below is just for testing - replace asap with working alternative
        Message m = new Message("communication", "", "", 0, 5, -1, "character enter");
        handleMessage(m, name);
    }

    @OnClose
    public void onClose(Session session, @PathParam("name") String name) {
        System.out.println("onClose> " + name);
        socketSessions.remove(session);

        int id = characters.getSocketCharacterId(name);
        Message m = new Message("communication", "", "", 0, id, -1, "character leave");
        handleMessage(m, name);
    }

    @OnError
    public void onError(Session session, @PathParam("name") String name, Throwable throwable) {
        System.out.println("onError> " + name + ": " + throwable);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("name") String name) {
        System.out.println("onMessage> " + name);
        Jsonb jsonb = JsonbBuilder.create();
        Message m = jsonb.fromJson(message, Message.class);
        if (m.seq >= 0) {
            Message compound = null;
            if (m.body.equals("~END~")) {
                compound = getSequencedMessage(m, name);
            } else {
                compound = messageSequence(m, name);
            }
            if (compound != null) {
                m = compound;
                this.finishedSequence.remove(m.getIdentifier(name));
                this.messageSequence.remove(m.getIdentifier(name));
                handleMessage(m, name);
            }
        } else {
            handleMessage(m, name);
        }
    }

    private Message messageSequence(Message m, String userId) {
        Message compound = null;
        String msgId = m.getIdentifier(userId);
        Message[] sequence = this.messageSequence.get(msgId);
        int seqLength = 0;
        if (sequence != null) {
            seqLength = sequence.length;
        }
        Message[] newSequence = new Message[Math.max(seqLength, m.seq + 1)];
        newSequence[m.seq] = m;
        if (sequence != null) {
            for (int i = 0; i < sequence.length; i ++) {
                if (i != m.seq) {
                    newSequence[i] = sequence[i];
                }
            }
        }
        this.messageSequence.put(msgId, newSequence);
        if (this.finishedSequence.get(msgId) == null) {
            this.finishedSequence.put(msgId, Boolean.FALSE);
        } else {
            if (this.finishedSequence.get(msgId)) {
                compound = getSequencedMessage(m, userId);
            }
        }

        return compound;
    }

    private Message getSequencedMessage(Message m, String userId) {
        String msgId = m.getIdentifier(userId);
        this.finishedSequence.put(msgId, Boolean.TRUE);
        Message[] sequence = this.messageSequence.get(msgId);
        Message compund = new Message(m.type, m.returnTo, m.modifier, m.code, m.charId, -1, "");
        for (int i = 0; i < sequence.length; i++) {
            if (sequence[i] == null) {
                return null;
            }
            compund.body += sequence[i].body;
        }

        return compund;
    }

    private void handleMessage(Message m, String name) {

        Jsonb jsonb = JsonbBuilder.create();
        MessageHandler handler = null;


        if (m.type.equals("communication")) {
            handler = new CommunicationHandler();
        } else if (m.type.equals("energy")) {
            handler = new EnergyHandler();
        } else if (m.type.equals("modValues")) {
            handler = new ModHandler();
        } else if (m.type.equals("testfile")) {
            handler = new TestFileHandler();
        }
        if (handler != null) {
            Envelope envelope = handler.handleMessage(m, name);
            if (envelope != null) {
                getSessionOfUser(envelope.reciever).getAsyncRemote().sendText(jsonb.toJson(envelope.message));
            }
        }





        /*
        Jsonb jsonb = JsonbBuilder.create();
        if (m.type.equals("titelbereich")) {
            Message answer = new Message();
            answer.type = "titelbereich";
            answer.code = 0;
            answer.seq = -1;
            JsonObject json = new JsonObject();
            json.put("aktLeP", 28);
            json.put("maxLeP", 30);
            json.put("ini", 11);
            json.put("gs", 7);
            answer.body = json.toString();
            getSessionOfUser(name).getAsyncRemote().sendText(jsonb.toJson(answer));
        }
        if (m.type.equals("titelbereich2")) {
            Message answer = new Message();
            answer.type = "titelbereich";
            answer.code = 1;
            answer.seq = -1;
            JsonObject json = new JsonObject();
            json = new JsonObject();
            json.put("aktLeP", 20);
            json.put("aw", 5);
            answer.body = json.toString();
            getSessionOfUser(name).getAsyncRemote().sendText(jsonb.toJson(answer));
        }
        if (m.type.equals("testfile")) {
            Message answer = new Message();
            answer.type = "hodensack";
            answer.code = 1;
            answer.seq = -1;
            answer.body = m.body;
            getSessionOfUser(name).getAsyncRemote().sendText(jsonb.toJson(answer));
        }
        */
    }
}
