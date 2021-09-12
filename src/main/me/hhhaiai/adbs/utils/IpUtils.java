package me.hhhaiai.adbs.utils;
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a variety of basic utility methods that are not
 * dependent on any other classes within the org.jamwiki package structure.
 */
public class IpUtils {
    private static Pattern VALID_IPV4_PATTERN = null;
    private static Pattern VALID_IPV4_PATTERN2 = null;
    private static Pattern VALID_IPV6_PATTERN = null;
    //    private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    private static final String ipv4Pattern =
            "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
    private static final String ipv4Pattern2 = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

    static {
        try {
            VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
            VALID_IPV4_PATTERN2 = Pattern.compile(ipv4Pattern2, Pattern.CASE_INSENSITIVE);
            VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Determine if the given string is a valid IPv4 or IPv6 address.  This method
     * uses pattern matching to see if the given string could be a valid IP address.
     *
     * @param ipAddress A string that is to be examined to verify whether or not
     *                  it could be a valid IP address.
     * @return <code>true</code> if the string is a value that is a valid IP address,
     * <code>false</code> otherwise.
     */
    public static boolean isIpAddress(String ipAddress) {

        Matcher m1 = IpUtils.VALID_IPV4_PATTERN.matcher(ipAddress);
        if (m1.matches()) {
            return true;
        }
        Matcher m2 = IpUtils.VALID_IPV6_PATTERN.matcher(ipAddress);
        return m2.matches();
    }

//    public static void main(String[] args) {
//        System.out.println(getIpv4("inet 192.168.50.111/24 brd 192.168.50.255 scope global wlan0"));
//        System.out.println(getIpv4(" 192.168.50.255 scope global wlan0"));
//        System.out.println(getIpv4("!254.254.254.254 127.0.0.1localhost192.168.2.1localhost 0.0.0.244. base 0.0.0.255.5;"));
//    }

    public static List<String> getIpv4(String ipinfo) {
        List<String> list = new ArrayList<String>();
        Matcher matcher = VALID_IPV4_PATTERN.matcher(ipinfo);
        if (matcher.groupCount() > 0) {
            System.out.println("[plan A]get ip. ");
            while (matcher.find()) {
                String ip = matcher.group();
                if (!list.contains(ip)) {
                    list.add(matcher.group());
                }
            }

        } else {
            matcher = VALID_IPV4_PATTERN2.matcher(ipinfo);
            if (matcher.groupCount() > 0) {
                System.out.println("[plan B]get ip.");
                while (matcher.find()) {
                    String ip = matcher.group();
                    if (!list.contains(ip)) {
                        list.add(matcher.group());
                    }
                }
            }

        }

        return list;
    }

}


