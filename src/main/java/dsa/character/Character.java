package dsa.character;

import dsa.services.ArrayListDeepCloner;

import java.util.ArrayList;

public class Character {
    private int id = -1;

    /**
     * Grundeigenschaften (MU, KL, ...)
     * Energien (LeP, AsP, ...)
     * Grundwerte (GS, ZK, ...)
     * Fähigkeiten, Zauber, ...
     * Stati (0 / 1)
     * Zustände
     * */
    private ArrayList<ModifiableValue> values = new ArrayList<>();

    private Profile profile;

    /**
     *
     * @return a <b>DEEP COPY</b> (not modifiable) of values
     */
    public ArrayList<ModifiableValue> getValues() {
        return ArrayListDeepCloner.deepClone(values);
    }

    /**
     *
     * @param modified
     * @param modifier
     * @return a <b>SHALLOW COPY</b> (modifiably) of the value
     */
    public ModifiableValue getValue(String modified, String modifier) {
        ModifiableValue wanted = null;
        for (ModifiableValue mv : values) {
            if (mv.equals(modified, modifier)) {
                wanted = mv;
            }
        }
        return wanted;
    }

    public Character(int charId, ArrayList<ModifiableValue> values, Profile profile) {
        this.id = charId;
        this.values = values;
        this.profile = profile;
    }

    /**
     *
     * @param name the modified value
     * @return
     */
    public int getModifiedByAll(String name) {
        int sum = 0;
        double factor = 1.0;
        for (ModifiableValue mv : values) {
            if (mv.modified.equalsIgnoreCase(name)) {
                if (mv.sumMod) {
                    sum += mv.modValue;
                } else {
                    factor *= mv.modValue;
                }
            }
        }
        return (int)Math.round((double)sum * factor);
    }

    public void addOrChangeModification(ModifiableValue value) {
        boolean exists = false;
        for (ModifiableValue mv: values) {
            if (mv.equals(value)) {
                exists = true;
                mv.copy(value);
            }
        }
        if (!exists) {
            values.add(value);
        }
    }

    public void deleteModification(ModifiableValue value) {
        ArrayList<ModifiableValue> values = new ArrayList<>();
        for (ModifiableValue mv: this.values) {
            if (!mv.equalsComplete(value)) {
                values.add(mv);
            }
        }
        this.values = values;
    }

    public int getGrundwert(String name) {
        ArrayList<String> modifier = new ArrayList<>();
        modifier.add("standard");
        return getModifiedBy(modifier, name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return profile.name;
    }


    /**
     *
     * @param modifier if <i>null</i> "standard" is chosen
     * @param name the modified value
     * @return
     */
    public int getModifiedBy(ArrayList<String> modifier, String name) {
        if (modifier == null) {
            modifier = new ArrayList<>();
            modifier.add("standard");
        }
        int sum = 0;
        double factor = 1.0;
        for (ModifiableValue mv : values) {
            // just apply modifier if it is one of the given modifiers
            if (mv.modified.equalsIgnoreCase(name)) {
                if (modifier.contains(mv.modifier)) {
                    if (mv.sumMod) {
                        sum += mv.modValue;
                    } else {
                        factor *= mv.modValue;
                    }
                }
            }
        }
        return (int)Math.round((double)sum * factor);
    }

    public ArrayList<ModifiableValue> getModifier(String modified) {
        ArrayList<ModifiableValue> list = new ArrayList<>();
        for (ModifiableValue mv : values) {
            if (mv.modified.equals(modified)) {
                list.add(mv);
            }
        }
        return list;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Character\n");
        sb.append(" profile\n");
        sb.append(profile);
        sb.append(" values\n");
        for (ModifiableValue mv : values) {
            sb.append("  " + mv + "\n");
        }
        return sb.toString();
    }
}
