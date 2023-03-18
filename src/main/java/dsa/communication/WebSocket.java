package dsa.communication;

import io.quarkus.vertx.http.runtime.devmode.Json;
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
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@ServerEndpoint("/ws/{name}")
@ApplicationScoped
public class WebSocket {

    private Map<String, Session> socketSessions = new HashMap<>();

    private Map<String, Message[]> messageSequence = new HashMap<>();
    private Map<String, Boolean> finishedSequence = new HashMap<>();

    public Session getSessionOfUser(String user) {
        return socketSessions.get(user);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name) {
        socketSessions.put(name, session);
        System.out.println("onOpen> " + name);
    }

    @OnClose
    public void onClose(Session session, @PathParam("name") String name) {
        socketSessions.remove(session);
        System.out.println("onClose> " + name);
    }

    @OnError
    public void onError(Session session, @PathParam("name") String name, Throwable throwable) {
        System.out.println("onError> " + name + ": " + throwable);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("name") String name) {
        Jsonb jsonb = JsonbBuilder.create();
        Message m = jsonb.fromJson(message, Message.class);
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
                // System.out.println(m);
                getSessionOfUser(name).getAsyncRemote().sendText(jsonb.toJson(m));
            }
        } else {
            // System.out.println(m);
            getSessionOfUser(name).getAsyncRemote().sendText(jsonb.toJson(m));
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
        Message compund = new Message(m.type, m.code, -1, "");
        for (int i = 0; i < sequence.length; i++) {
            if (sequence[i] == null) {
                return null;
            }
            compund.body += sequence[i].body;
        }
        return compund;
    }
}
