package dsa.communication;

import dsa.character.Character;
import dsa.db.DBCharacter;
import dsa.db.DBCharakter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CharacterStorage {
    private DBCharacter db = DBCharacter.singleton();
    private static CharacterStorage exemplar = null;
    private CharacterStorage() {}

    private Map<Integer, Character> characters = new HashMap<>();
    private Map<String, Integer> socketCharacters = new HashMap<>();

    public static CharacterStorage singleton() {
        if (exemplar == null) {
            exemplar = new CharacterStorage();
        }
        return exemplar;
    }

    public void putSocketCharacterId(String socketId, int charId) {
        socketCharacters.put(socketId, charId);
    }
    public int getSocketCharacterId(String socketId) {
        return socketCharacters.get(socketId);
    }
    public int removeSocketCharacter(String socketId) {
        return socketCharacters.remove(socketId);
    }

    public ArrayList<Character> getAllCharacters() {
        return new ArrayList<>(characters.values());
    }

    public Character getCharacter(int charId) {
        Character character = characters.get(charId);
        if (character == null) {
            character = db.loadCharacter(charId);
            characters.put(charId, character);
        }
        return character;
    }

    public void removeCharacter(int charID) {
        characters.remove(charID);
    }

    public ArrayList<String> saveCharacter(int id) {
        System.out.println("save character " + id);
        ArrayList<String> meldungen = new ArrayList<>();
        Character character = getCharacter(id);
        if (character != null) {
            meldungen = db.saveCharacter(getCharacter(id));
        } else {
            meldungen.add("Character with id " + id + " is null!");
        }
        return meldungen;
    }

}
