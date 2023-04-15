package dsa.communication;

public class TitelbereichHandler extends MessageHandler {

    @Override
    protected Envelope handleMessage(Message msg) {
        System.out.println(msg.body);
        return null;
    }
}
