package dsa.communication;

public class TestFileHandler extends MessageHandler{

    @Override
    protected Envelope handleMessage(Message msg, String name) {
        Envelope envelope = new Envelope(name, name);
        envelope.message = msg;
        return envelope;
    }
}
