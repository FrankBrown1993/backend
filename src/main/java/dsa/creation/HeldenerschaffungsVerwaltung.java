package dsa.creation;

import java.util.HashMap;

public class HeldenerschaffungsVerwaltung {
    public HashMap<String, Heldenerschaffung> map = new HashMap<>();

    private HeldenerschaffungsVerwaltung() {}
    private static HeldenerschaffungsVerwaltung exemplar = null;
    public static HeldenerschaffungsVerwaltung singleton() {
        if (exemplar == null) {
            exemplar = new HeldenerschaffungsVerwaltung();
        }
        return exemplar;
    }

    public void startErschaffung(String username) {
        map.put(username, new Heldenerschaffung());
    }


}
