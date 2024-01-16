package dsa.communication;

/**
 * Diese Klasse dient als Briefumschlag. Sie enthält Informationen zu <b>Sender</b>,
 * <b>Empfänger</b> und die <b>Nachricht</b> selbst.
 */
public class Envelope {
    protected String sender;
    protected String reciever;
    protected Message message;

    public Envelope() {
    }

    public Envelope(String sender, String reciever) {
        this.sender = sender;
        this.reciever = reciever;
    }
}
