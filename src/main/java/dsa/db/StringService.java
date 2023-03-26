package dsa.db;

public class StringService {
    public static String escapeApostrophs(String str) {
        str = str.replaceAll("'", "''");
        return str;
    }
}
