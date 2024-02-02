package dsa.communication;

public class Message {
    public String type;
    public String returnType;
    public String modifier;
    public int code;
    public int charId;
    public String body;
    public int seq;

    public Message() { }

    public Message(String type, String returnType, String modifier, int code, int charId, int seq, String body) {
        this.type = type;
        this.returnType = returnType;
        this.modifier = modifier;
        this.code = code;
        this.charId = charId;
        this.seq = seq;
        this.body = body;
    }

    @Override
    public String toString() {
        String out = "type: " + type + "\n";
        out += "returnTo: " + returnType + "\n";
        out += "modifier: " + modifier + "\n";
        out += "code: " + code + "\n";
        out += "charId: " + charId + "\n";
        out += "seq: " + seq + "\n";
        out += "body: " + body;
        return out;
    }

    public void printHeader() {
        String print = ">>> Message start <<<\n";
        print += "type: " + type + "\n";
        print += "returnTo: " + returnType + "\n";
        print += "modifier: " + modifier + "\n";
        print += "code: " + code + "\n";
        print += "charId: " + charId + "\n";
        print += ">>> Message end <<<";
        System.out.println(print);
    }

    public String getIdentifier(String userId) {
        return "[" + userId + "],[type: " + type + "]," +
                "[returnTo: " + returnType + "]," +
                "[modifier: " + modifier + "]," +
                "[code: " + code + "]";
    }
}
