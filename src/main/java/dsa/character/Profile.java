package dsa.character;

public class Profile {
    public String name;
    public String geschlecht;
    public String tsaTag;
    public String spezies;
    public String kultur;
    public String profession;
    public String haarfarbe;
    public String augenfarbe;
    public String schamhaare;
    public String brueste;
    public String genital;
    public int alter;
    public int groesse;
    public int gewicht;
    public String titel;
    public String portrait;

    public Profile() {}

    public Profile(Profile other) {
        this.name = other.name;
        this.geschlecht = other.geschlecht;
        this.tsaTag = other.tsaTag;
        this.spezies = other.spezies;
        this.kultur = other.kultur;
        this.profession = other.profession;
        this.haarfarbe = other.haarfarbe;
        this.augenfarbe = other.augenfarbe;
        this.schamhaare = other.schamhaare;
        this.brueste = other.brueste;
        this.genital = other.genital;
        this.alter = other.alter;
        this.groesse = other.groesse;
        this.gewicht = other.gewicht;
        this.titel = other.titel;
        this.portrait = other.portrait;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("  name: ");
        sb.append(name + "\n");
        sb.append("  geschlecht: ");
        sb.append(geschlecht + "\n");
        sb.append("  tsaTag: ");
        sb.append(tsaTag + "\n");
        sb.append("  spezies: ");
        sb.append(spezies + "\n");
        sb.append("  kultur: ");
        sb.append(kultur + "\n");
        sb.append("  profession: ");
        sb.append(profession + "\n");
        sb.append("  haarfarbe: ");
        sb.append(haarfarbe + "\n");
        sb.append("  augenfarbe: ");
        sb.append(augenfarbe + "\n");
        sb.append("  schamhaare: ");
        sb.append(schamhaare + "\n");
        sb.append("  brueste: ");
        sb.append(brueste + "\n");
        sb.append("  genital: ");
        sb.append(genital + "\n");
        sb.append("  alter: ");
        sb.append(alter + "\n");
        sb.append("  groesse: ");
        sb.append(groesse + "\n");
        sb.append("  gewicht: ");
        sb.append(gewicht + "\n");
        sb.append("  titel: ");
        sb.append(titel + "\n");
        return sb.toString();
    }
}
