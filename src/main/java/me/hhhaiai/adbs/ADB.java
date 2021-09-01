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


//        List<String> permissions = getPermissions();
////        System.out.println(permissions.toString());
//        for (String l : permissions) {
//            System.out.println(l);
//        }

//        System.out.println(getAllPkgs());

        pmGrant();
    }

    public static void pmGrant() {
        pmGrant("");
    }

    /**
     * 授予权限
     *
     * @param deviceId
     * @return
     */
    public static void pmGrant(String deviceId) {
        // pm
        // -g: grant all runtime permissions
        // grant [--user USER_ID] PACKAGE PERMISSION
        //  revoke [--user USER_ID] PACKAGE PERMISSION
        //    These commands either grant or revoke permissions to apps.  The permissions
        //    must be declared as used in the app's manifest, be runtime permissions
        //    (protection level dangerous), and the app targeting SDK greater than Lollipop MR1.
        List<String> pkgs = getAllPkgs();
        List<String> pers = getPermissions();
//        Logs.d("pkgs:" + pkgs.size() + "-----pers:" + pers.size());
        for (String pkg : pkgs) {
            for (String per : pers) {
                ShellUtils.getArrayUseAdbShell(deviceId, "pm grant " + pkg + " " + per);
//                FileUtils.saveTextToFile("/Users/sanbo/Desktop/a.txt","pm grant " + pkg + " " + per,true);
            }
        }

    }

    public static List<String> getAllPkgs() {
        return getAllPkgs("");
    }

    public static List<String> getAllPkgs(String deviceId) {
        // pm
        // -g: grant all runtime permissions
        // grant [--user USER_ID] PACKAGE PERMISSION
        //  revoke [--user USER_ID] PACKAGE PERMISSION
        //    These commands either grant or revoke permissions to apps.  The permissions
        //    must be declared as used in the app's manifest, be runtime permissions
        //    (protection level dangerous), and the app targeting SDK greater than Lollipop MR1.
        return ShellUtils.getArrayUseAdbShell(deviceId, "pm list packages", "package:", "");
    }

    /**
     * 查看设备上已经有的权限，包括系统定义和应用自定义
     *
     * @return
     */
    public static List<String> getPermissions() {
        return getPermissions("");
    }

    /**
     * 查看设备上已经有的权限，包括系统定义和应用自定义
     *
     * @param deviceId adb devices获取的设备ID
     * @return
     */
    public static List<String> getPermissions(String deviceId) {
        // adb shell pm list permissions
        CopyOnWriteArrayList<String> permissions = ShellUtils.getArrayUseAdbShell(deviceId, "pm list permissions", "permission:", "");
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
