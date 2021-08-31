package me.hhhaiai.adbs;

import me.hhhaiai.adbs.utils.ShellUtils;
import me.hhhaiai.adbs.utils.android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Copyright © 2021 sanbo Inc. All rights reserved.
 * @Description: adb 命令工具,即将用于支持shell的指令
 * @Version: 1.0
 * @Create: 2021/08/31 15:20:59
 * @author: sanbo
 */
public class ADB {
    public static void main(String[] args) {
//        List<String> devices = devices();
//        Logs.i("Device list:" + devices.toString());


        List<String> permissions = getPermissions();
//        System.out.println(permissions.toString());
        for (String l : permissions) {
            System.out.println(l);
        }
    }

    /**
     * 查看设备上已经有的权限，包括系统定义和应用自定义
     *
     * @return
     */
    private static List<String> getPermissions() {
        // adb shell pm list permissions
        CopyOnWriteArrayList<String> permissions = ShellUtils.getArrayUseAdbShell("pm list permissions", "permission:", "");
        List<String> result = new CopyOnWriteArrayList<String>(permissions);
        for (String permission : permissions) {
            if (TextUtils.isEmpty(permission)) {
                continue;
            }
            if (permission.startsWith("All")) {
                result.remove(permission);
            }
        }
        return result;
    }

    /**
     * 获取设备列表
     *
     * @return
     */
    public static List<String> devices() {
        List<String> devices = new ArrayList<String>();
        List<String> ts = ShellUtils.getArrayUseAdb("devices");
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
