package me.hhhaiai.adbs.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacHelper {
    public static void main(String[] args) {
        String line = "link/ether 02:15:b2:00:00:00 brd ff:ff:ff:ff:ff:ff";
        String mac = "02:15:b2:00:00:00";
//        System.out.println(getMac("link/ether 02:15:b2:00:00:00 brd ff:ff:ff:ff:ff:ff"));

//        System.out.println( validateMAC("02:15:b2:00:00:00"));
        System.out.println(isValidMacAddress(mac));

        try {
            System.out.println("1: "+patternMacPairs.matcher(mac).find());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            System.out.println("2: "+patternMacTriples.matcher(mac).find());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            System.out.println(Pattern.compile(
                            "^([a-fA-F0-9]{2}[:-]){5}[a-fA-F0-9]{2}$"
                    )
                    .matcher(line).find());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            System.out.println(Pattern.compile("^([a-fA-F0-9]{2}[:-]){5}[a-fA-F0-9]{2}$").matcher(line).find());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            System.out.println(patternMacPairs.matcher(line).find());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            System.out.println(patternMacTriples.matcher(line).find());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            System.out.println(
                    Pattern.compile("^([0-9A-Fa-f]{2}:){5}(([0-9A-Fa-f]{2}:){14})?([0-9A-Fa-f]{2})$"
                    ).matcher(line).find());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static Pattern patternMacPairs = Pattern.compile("^([a-fA-F0-9]{2}[:\\.-]?){5}[a-fA-F0-9]{2}$");
    private static Pattern patternMacTriples = Pattern.compile("^([a-fA-F0-9]{3}[:\\.-]?){3}[a-fA-F0-9]{3}$");

    private static boolean isValidMacAddress(String mac) {
        // Mac addresses usually are 6 * 2 hex nibbles separated by colons,
        // but apparently it is legal to have 4 * 3 hex nibbles as well,
        // and the separators can be any of : or - or . or nothing.
        return (patternMacPairs.matcher(mac).find() || patternMacTriples.matcher(mac).find());
    }

    private static final Pattern MAC_PATTERN =
            Pattern.compile("^([0-9A-Fa-f]{2}[\\.:-]){5}([0-9A-Fa-f]{2})$");
    private static final String PATTERN = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";

    //    private static boolean validateMAC(String mac) {
//        Pattern pattern = Pattern.compile(PATTERN);
//        Matcher matcher = pattern.matcher(mac);
//        return matcher.matches();
//    }
    public static String getMac(String line) {
        System.out.println(line);
//        Matcher matcher = MAC_PATTERN.matcher(line);
        Matcher matcher = Pattern.compile(PATTERN).matcher(line);
        System.out.println(matcher.groupCount());
        System.out.println(matcher.find());
//        if (matcher.groupCount() > 0) {
//            System.out.println("find:" +matcher.find());
//            while (matcher.find()) {
//                String res = matcher.group();
//                System.out.println(res);
//                if (!"ff:ff:ff:ff:ff:ff".equals(res)) {
//                    return res;
//                }
//            }
//        }
        return "";
    }
}
