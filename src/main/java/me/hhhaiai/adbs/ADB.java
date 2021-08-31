package me.hhhaiai.adbs;

import me.hhhaiai.adbs.utils.ShellUtils;
import me.hhhaiai.adbs.utils.android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright © 2021 sanbo Inc. All rights reserved.
 * @Description: adb 命令工具
 * @Version: 1.0
 * @Create: 2021/08/31 15:20:59
 * @author: sanbo
 */
public class ADB {
    public static void main(String[] args) {

        List<String> devices = devices();
        System.out.println(devices.toString());
    }

    /**
     * 获取设备列表
     *
     * @return
     */
    public static List<String> devices() {
        List<String> devices = new ArrayList<String>();
        List<String> ts = ShellUtils.getArrayUseAdb("adb devices");
        if (ts == null || ts.size() == 0) {
            return devices;
        }
        for (String line : ts) {

            if (TextUtils.isEmpty(line)) {
                continue;
            }
            // default result
            // List of devices attached
            // 88Y5T19B26003875	device
            if (line.startsWith("List")) {
                continue;
            }
            if (line.contains("\t")) {
                String[] ss = line.split("\t");
                if (ss.length == 2) {
                    devices.add(ss[0]);
                }
            }
        }
        return devices;
    }
}
