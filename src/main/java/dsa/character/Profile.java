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
