package dsa.character;

public class ModifiableValue {
    public String category;
    public String modified;
    public String modifier;
    public float modValue;
    public boolean sumMod;

    public ModifiableValue() {};

    public ModifiableValue(ModifiableValue other) {
        this.sumMod = other.sumMod;
        this.modified = other.modified;
        this.modifier = other.modifier;
        this.modValue = other.modValue;
        this.category = other.category;
    }

    public void copy(ModifiableValue other) {
        this.sumMod = other.sumMod;
        this.modified = other.modified;
        this.modifier = other.modifier;
        this.modValue = other.modValue;
        this.category = other.category;
    }

    public ModifiableValue(String category, String modified, String modifier, float modValue, boolean sumMod) {
        this.category = category;
        this.modified = modified;
        this.modifier = modifier;
        this.modValue = modValue;
        this.sumMod = sumMod;
    }

    public boolean equals(String modified, String modifier) {
        boolean modifiedEqual = false;
        if (this.modified == null || modified == null) {
            modifiedEqual = this.modified == modified;
        } else {
            modifiedEqual = this.modified.equalsIgnoreCase(modified);
        }

        boolean modifierEqual = false;
        if (this.modifier == null || modifier == null) {
            modifierEqual = this.modifier == modifier;
        } else {
            modifierEqual = this.modifier.equalsIgnoreCase(modifier);
        }

        return modifiedEqual && modifierEqual;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ModifiableValue)) return false;
        ModifiableValue other = (ModifiableValue) obj;
        return equals(other.modified, other.modifier);
    }

    public boolean equalsComplete(Object obj) {
        if (obj == null || !(obj instanceof ModifiableValue)) return false;
        ModifiableValue other = (ModifiableValue) obj;

        boolean catEqual = false;
        if (this.category == null || other.category == null) {
            catEqual = this.category == other.category;
        } else {
            catEqual = this.category.equalsIgnoreCase(other.category);
        }

        boolean modifiedEqual = false;
        if (this.modified == null || other.modified == null) {
            modifiedEqual = this.modified == other.modified;
        } else {
            modifiedEqual = this.modified.equalsIgnoreCase(other.modified);
        }

        boolean modifierEqual = false;
        if (this.modifier == null || other.modifier == null) {
            modifierEqual = this.modifier == other.modifier;
        } else {
            modifierEqual = this.modifier.equalsIgnoreCase(other.modifier);
        }

        boolean modValueEqual = this.modValue == other.modValue;
        boolean sumModEqual = this.sumMod == other.sumMod;
        return catEqual && modifiedEqual && modifierEqual && modValueEqual && sumModEqual;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MV(");
        sb.append("category: ");
        sb.append(category);
        sb.append(", modified: ");
        sb.append(modified);
        sb.append(", modifier: ");
        sb.append(modifier);
        sb.append(", modValue: ");
        sb.append(modValue);
        sb.append(", sumMod: ");
        sb.append(sumMod);
        sb.append(")");
        return sb.toString();
    }
}
