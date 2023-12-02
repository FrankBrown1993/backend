package dsa.services;

import dsa.character.ModifiableValue;
import dsa.db.DBCharacter;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CharacterComparator {
    private static CharacterComparator exemplar = null;
    public static CharacterComparator singleton() {
        if (exemplar == null) {
            exemplar = new CharacterComparator();
        }
        return exemplar;
    }

    /**
     * examines the differences between the <b>ModifiableValue</b> data from the
     * character and the data in the DB and returns <b>new</b>, <b>updated</b> and <b>deleted</b> values
     * @param cMV modifiedValues of <b>Character</b>
     * @param dbMV modifiedValues of <b>DB</b>
     * @return ArrayList[] ={<br>
     *     ArrayList&lt;ModifiableValue&gt; <b>newValues</b>,<br>
     *     ArrayList&lt;ModifiableValue&gt; <b>updatedValues</b>,<br>
     *     ArrayList&lt;ModifiableValue&gt; <b>deletedValues</b><br>
     *     }
     */
    public ArrayList[] compareModifiableValues(
        ArrayList<ModifiableValue> cMV, ArrayList<ModifiableValue> dbMV) {
        ArrayList[] list = new ArrayList[3];
        ArrayList<ModifiableValue> newValues = ArrayListDeepCloner.deepClone(cMV);
        ArrayList<ModifiableValue> updatedValues = new ArrayList<>();
        ArrayList<ModifiableValue> deletedValues = ArrayListDeepCloner.deepClone(dbMV);

        // values that are present in character but not in DB
        newValues.removeAll(dbMV);

        // values that are present in both but differ
        for (ModifiableValue mv : cMV) {
            for (ModifiableValue other: dbMV) {
                // check if they should be the same (by primary key)
                if (mv.equals(other)) {
                    // check if they differ
                    if (!mv.equalsComplete(other)) {
                        updatedValues.add(mv);
                    }
                    break;
                }
            }
        }

        // values that are present in DB but not in character
        deletedValues.removeAll(cMV);

        list[0] = newValues;
        list[1] = updatedValues;
        list[2] = deletedValues;
        return list;
    }
}
