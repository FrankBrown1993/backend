package dsa.db;

public class StringChecker {
    public static String convertNullOrEmpty(String s) {
        String str = s;
        if (s != null) {
            if (s.equalsIgnoreCase("null")) {
                str = null;
            }
            if (s.trim().length() == 0) {
                str = null;
            }
        }
        return str;
    }
}
