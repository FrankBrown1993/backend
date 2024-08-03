package dsa.services;

import dsa.character.ModifiableValue;

import java.util.ArrayList;

public class ArrayListDeepCloner {
    public static ArrayList<ModifiableValue> deepClone(ArrayList<ModifiableValue> list) {
        ArrayList<ModifiableValue> clone = new ArrayList<>();
        for (ModifiableValue mv : list) {
            clone.add(new ModifiableValue(mv));
        }
        return clone;
    }
}
