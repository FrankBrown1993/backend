package dsa.kampf;

import dsa.db.DBCharacter;

import java.util.ArrayList;

public class Arena {
    private Arena() {}
    private static Arena exemplar = null;
    public static Arena singleton() {
        if (exemplar == null) {
            exemplar = new Arena();
        }
        return exemplar;
    }
    public ArrayList<Fighter> fighters = new ArrayList<>();

    public boolean isCharInArena(int id) {
        boolean isInArena = false;
        for (Fighter fighter : fighters) {
            if(fighter.id == id) {
                isInArena = true;
            }
        }
        return isInArena;
    }

}
