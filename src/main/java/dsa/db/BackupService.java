package dsa.db;

import dsa.character.Character;
import dsa.communication.CharacterStorage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.TimerTask;

public class BackupService extends TimerTask {
    private CharacterStorage storage = CharacterStorage.singleton();
    DBCharakter db = DBCharakter.singleton();

    @Override
    public void run() {
        String time = "" + LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledExecutionTime()), ZoneId.systemDefault());
        System.out.println("[BackupService]: " + time + " backing up characters");
        backup();
    }

    public synchronized ArrayList<String> backup() {
        ArrayList<String> errors = new ArrayList<>();
        for (Character c : storage.getAllCharacters()) {

        }
        return errors;
    }
}
