package dsa.communication;

import java.util.ArrayList;

public class TestFileHandler extends MessageHandler {

    @Override
    protected ArrayList<Envelope> handleMessage(Message msg, String name) {
        Envelope envelope = new Envelope(name, name);
        envelope.message = msg;
        ArrayList<Envelope> envelopes = new ArrayList<>();
        envelopes.add(envelope);
        return envelopes;
    }
}
