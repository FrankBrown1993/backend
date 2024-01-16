package dsa.character;

import dsa.services.ArrayListDeepCloner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Character {
    private int id = -1;

    // both dictionaries must be lower case!
    private List<String> dictWords = Arrays.asList("erschoepfung");
    private List<String> dictRplce = Arrays.asList("erschöpfung");

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

    private ArrayList<Beziehung> beziehungen = new ArrayList<>();

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

    public Character(int charId, ArrayList<ModifiableValue> values, Profile profile, ArrayList<Beziehung> beziehungen) {
        this.id = charId;
        this.values = values;
        this.profile = profile;
        this.beziehungen = beziehungen;
    }

    /**
     *
     * @param name the modified value
     * @return
     */
    public int getModifiedByAll(String name) {
        int index = dictWords.indexOf(name.toLowerCase());
        if (index >= 0) {
            name = dictRplce.get(index);
        }
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
        int index = dictWords.indexOf(name.toLowerCase());
        if (index >= 0) {
            name = dictRplce.get(index);
        }
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

    public String getPortrait() { return profile.portrait; }
    public void setPortrait(String portrait) { this.profile.portrait = portrait; }

    /**
     * FUNCTION
     * @return a copy of the profile
     */
    public Profile getProfile() {
        return new Profile(profile);
    }


    /**
     *
     * @param modifier if <i>null</i> "standard" is chosen
     * @param name the modified value
     * @return
     */
    public int getModifiedBy(ArrayList<String> modifier, String name) {
        int index = dictWords.indexOf(name.toLowerCase());
        if (index >= 0) {
            name = dictRplce.get(index);
        }
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
        int index = dictWords.indexOf(modified.toLowerCase());
        if (index >= 0) {
            modified = dictRplce.get(index);
        }
        ArrayList<ModifiableValue> list = new ArrayList<>();
        for (ModifiableValue mv : values) {
            if (mv.modified.equalsIgnoreCase(modified)) {
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
