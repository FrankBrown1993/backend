package dsa.communication;

public class Message {
    public String type;
    public int code;
    public String body;
    public int seq;

    public Message() { }
    public Message(String type, int code, int seq, String body) {
        this.type = type;
        this.code = code;
        this.seq = seq;
        this.body = body;
    }

    @Override
    public String toString() {
        String out = "type: " + type + "\n";
        out += "code: " + code + "\n";
        out += "seq: " + seq + "\n";
        out += "body: " + body;
        return out;
    }

    public String getIdentifier(String userId) {
        return "[" + userId + "],[type: " + type + "],[code: " + code + "]";
    }
}
