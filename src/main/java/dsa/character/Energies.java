package dsa.character;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents the energies of a character like <b>LeP</b>, <b>AsP</b> or <b>KaP</b>.
 */
public class Energies {
    private HashMap<String, Value> energies = new HashMap<>();

    public Energies() {
        String[] names = {"LeP", "AsP", "KaP"};
        for (String name : Arrays.asList(names)) {
            energies.put(name, new Value(name));
        }
    }
}
