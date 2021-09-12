package me.hhhaiai.adbs.utils;

import me.hhhaiai.adbs.utils.android.text.TextUtils;

import java.util.List;

public class MacHelper {

    String line = "link/ether 02:15:b2:00:00:00 brd ff:ff:ff:ff:ff:ff";
    String mac = "02:15:b2:00:00:00";
    //单个识别没问题，抽取失败
    String pattern = "^([A-Fa-f0-9]{2}\\:){5}[A-Fa-f0-9]{2}$";

    public static void main(String[] args) {
        List<String> macInfos = ShellUtils.getArrayUseAdbShell("", "ip link");
//        System.out.println(macInfos.size());
        for (int i = 0; i < macInfos.size(); i++) {
            String line = macInfos.get(i);
//            System.out.println(line);
            if (!TextUtils.isEmpty(line) && line.length() > 9) {
                String temp = line.substring(0, 10);

                if (!TextUtils.isEmpty(temp)) {
                    if (temp.contains("wlan0")) {
                        String nextLine = macInfos.get(i + 1);
//                        System.err.println("随机:" + line);
//                        System.err.println("随机mac行:" + nextLine);
                        System.err.println("随机mac:" + MacHelper.getMac(nextLine));
                    } else if (temp.contains("wlan1")) {
                        String nextLine = macInfos.get(i + 1);
//                        System.err.println("真实:" + line);
//                        System.err.println("真实mac行:" + nextLine);
                        System.err.println("真实mac:" + MacHelper.getMac(nextLine));
                    }
                }
            }

        }
    }

    public static String getMac(String line) {
        if (!TextUtils.isEmpty(line) && line.contains(" ")) {
            String[] ss = line.split(" ");
            if (ss != null && ss.length > 0) {
                for (int i = 0; i < ss.length; i++) {
                    String one = ss[i];
                    if (!TextUtils.isEmpty(one) && one.contains(":") && !"ff:ff:ff:ff:ff:ff".equals(one)) {
                        return one;
                    }
                }
            }
        }
        return "";
    }
}
