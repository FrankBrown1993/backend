package dsa.communication;

/**
 * Diese Klasse kümmert sich um die Behandlung der allgemeinen Nachrichten.
 * Falls es nötig ist Websockets zu Kontaktieren, wird eine Nachricht in einen
 * Umschlag (Envelope) gesteckt und an den Websocket zurückgegeben.
 */
public class MessageHandler {
    protected Envelope handleMessage(Message msg) {
        return null;
    }
}
