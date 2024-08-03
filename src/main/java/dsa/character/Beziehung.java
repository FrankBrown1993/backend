package dsa.character;

public class Beziehung {
    private String name;

    private int freundschaft;

    private int romantik;

    public Beziehung(String name, int freundschaft, int romantik) {
        this.name = name;
        this.freundschaft = freundschaft;
        this.romantik = romantik;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFreundschaft() {
        return freundschaft;
    }

    public void setFreundschaft(int freundschaft) {
        this.freundschaft = freundschaft;
    }

    public int getRomantik() {
        return romantik;
    }

    public void setRomantik(int romantik) {
        this.romantik = romantik;
    }
}
