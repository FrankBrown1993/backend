package dsa.character;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents the base attributes of the character.<br/>
 * These are <b>MU</b>, <b>KL</b>, <b>CH</b>, <b>IN</b>, <b>FF</b>
 * <b>GE</b>, <b>KO</b> and <b>KK</b>.
 */
public class Attributes {
    private HashMap<String, Value> attributes = new HashMap<>();

    public Attributes() {
        String[] names = {"MU", "KL", "IN", "CH", "FF", "GE", "KO", "KK"};
        for (String name : Arrays.asList(names)) {
            attributes.put(name, new Value(name));
        }
    }
}
