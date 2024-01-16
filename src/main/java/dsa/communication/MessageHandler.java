package dsa.communication;

import dsa.character.Character;
import dsa.communication.CharacterStorage;
import dsa.communication.Envelope;
import dsa.communication.Message;

/**
 * Diese Klasse kümmert sich um die Behandlung der allgemeinen Nachrichten.
 * Falls es nötig ist Websockets zu Kontaktieren, wird eine Nachricht in einen
 * Umschlag (Envelope) gesteckt und an den Websocket zurückgegeben.
 */
public abstract class MessageHandler {
    protected CharacterStorage characters = CharacterStorage.singleton();
    protected abstract Envelope handleMessage(Message msg, String name);
}
