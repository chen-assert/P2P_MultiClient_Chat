package src;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TEST {
    public static void main(String[] args) {
        String pattern = "BROADCAST\\s*-\\s*(.*)\\s*";
        String line = "BRoADCAST - qwe";
        Pattern r = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(line);
        m.find();
        System.out.print(m.group(0));
    }
}
