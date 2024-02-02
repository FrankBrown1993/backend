package dsa.kampf;

import dsa.character.Character;

public class Fighter {
    public int id;
    public String name;
    public String token;
    public String portrait;
    public int basisIni;
    public int aktIni;
    public Position position;
    public double gsBasis;
    public double gsAkt;
    public double movement;

    public Fighter(int id, String name, String token, String portrait, int basisIni, int aktIni,
                   Position position, double gsBasis, double gsAkt, double movement) {
        this.id = id;
        this.name = name;
        this.token = token;
        this.portrait = portrait;
        this.basisIni = basisIni;
        this.aktIni = aktIni;
        this.position = position;
        this.gsBasis = gsBasis;
        this.gsAkt = gsAkt;
        this.movement = movement;
    }

    public Fighter(Character character, Position pos) {
        this.id = character.getId();
        this.name = character.getName();
        this.token = character.getToken();
        this.portrait = character.getPortrait();
        this.basisIni = character.getGrundwert("INI");
        this.aktIni = character.getModifiedByAll("INI");
        this.position = pos;
        this.gsBasis = character.getGrundwert("GS");
        this.gsAkt = character.getModifiedByAll("GS");
        this.movement = this.gsBasis;
    }
}
