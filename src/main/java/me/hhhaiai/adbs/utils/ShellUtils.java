package me.hhhaiai.adbs.utils;

import me.hhhaiai.adbs.utils.android.text.TextUtils;

import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Copyright © 2021 sanbo Inc. All rights reserved.
 * @Description: 优化调整Java版本shell工具类，该版本支持一级指令和二级单独指令
 * @Version: 2.0
 * @Create: 2021/08/31 18:23:56
 * @author: sanbo
 */
public class ShellUtils {

    public static String getString(String cmd) {
        return (String) getBaseAppend(false, cmd, null, null, null, null);
    }

    public static CopyOnWriteArrayList<String> getArray(String cmd) {
        return (CopyOnWriteArrayList<String>) getBaseAppend(true, cmd, null, null, null, null);
    }

    public static String getStringBysh(String subCommand) {
        return (String) getBaseAppend(false, "sh", null, subCommand, null, null);
    }

    public static CopyOnWriteArrayList<String> getArrayBysh(String subCommand) {
        return (CopyOnWriteArrayList<String>) getBaseAppend(true, "sh", null, subCommand, null, null);
    }

    /**
     * 支持 adb devices系列指令
     *
     * @param appendBase
     * @return
     */
    public static String getStringUseAdb(String appendBase) {
        return (String) getBaseAppend(false, "adb", appendBase, null, null, null);
    }

    /**
     * 支持 adb devices系列指令,可替换部分内容
     *
     * @param appendBase
     * @param target
     * @param replacement
     * @return
     */
    public static String getStringUseAdb(String appendBase, CharSequence target, CharSequence replacement) {
        return (String) getBaseAppend(false, "adb", appendBase, null, target, replacement);
    }

    /**
     * 支持adb shell->xxx 单个二级指令
     *
     * @param subCommand
     * @return
     */
    public static String getStringUseAdbShell(String subCommand) {
        return (String) getBaseAppend(false, "adb", "shell", subCommand, null, null);
    }

    /**
     * 支持adb shell->xxx 单个二级指令，可替换部分内容
     *
     * @param subCommand
     * @return
     */
    public static String getStringUseAdbShell(String subCommand, CharSequence target, CharSequence replacement) {
        return (String) getBaseAppend(false, "adb", "shell", subCommand, target, replacement);
    }

    /**
     * 支持 adb devices系列指令
     *
     * @param appendBase
     * @return
     */
    public static CopyOnWriteArrayList<String> getArrayUseAdb(String appendBase) {
        return (CopyOnWriteArrayList<String>) getBaseAppend(true, "adb", appendBase, null, null, null);
    }

    /**
     * 支持 adb devices系列指令,可替换部分内容
     *
     * @param appendBase
     * @param target
     * @param replacement
     * @return
     */
    public static CopyOnWriteArrayList<String> getArrayUseAdb(String appendBase, CharSequence target, CharSequence replacement) {
        return (CopyOnWriteArrayList<String>) getBaseAppend(true, "adb", appendBase, null, target, replacement);
    }

    /**
     * 支持adb shell->xxx 单个二级指令
     *
     * @param subCommand
     * @return
     */
    public static CopyOnWriteArrayList<String> getArrayUseAdbShell(String subCommand) {
        return (CopyOnWriteArrayList<String>) getBaseAppend(true, "adb", "shell", subCommand, null, null);
    }

    /**
     * 支持adb shell->xxx 单个二级指令，可替换部分内容
     *
     * @param subCommand
     * @return
     */
    public static CopyOnWriteArrayList<String> getArrayUseAdbShell(String subCommand, CharSequence target, CharSequence replacement) {
        return (CopyOnWriteArrayList<String>) getBaseAppend(true, "adb", "shell", subCommand, target, replacement);
    }


    /**
     * 基础工具方法
     *
     * @param isResultArray 返回值只有 CopyOnWriteArrayList/String， boolean值代表非此即彼
     * @param base          基础命令. 如adb
     * @param appendBase    基于基础指令的追加命令. 如该参数为devices，则执行指令 adb devices
     * @param cmd           二级子命令. 如 getprop.  综合之前，就会出现 一级指令：adb shell, 二级指令：getprop
     * @param target        The sequence of char values to be replaced  被替换内容
     * @param replacement   The replacement sequence of char values   替换目标内容
     * @return
     */
    private static Object getBaseAppend(boolean isResultArray, String base, String appendBase, String cmd, CharSequence target, CharSequence replacement) {
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        StringBuilder resultSb = new StringBuilder();
        DataOutputStream os = null;
        OutputStream pos = null;
        CopyOnWriteArrayList<String> resultsArray = new CopyOnWriteArrayList<>();
        try {
            String bs = (TextUtils.isEmpty(base) ? "" : base) + (TextUtils.isEmpty(appendBase) ? "" : " " + appendBase);
            proc = Runtime.getRuntime().exec(bs);
            pos = proc.getOutputStream();
            if (!TextUtils.isEmpty(cmd)) {
                os = new DataOutputStream(pos);

                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(cmd.getBytes());
                os.writeBytes("\n");
                os.flush();
                //exitValue
                os.writeBytes("exit\n");
                os.flush();
            }
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (isResultArray) {
                    if (!TextUtils.isEmpty(line)) {
                        if (TextUtils.isEmpty(target) && TextUtils.isEmpty(replacement)) {
                            resultsArray.add(line);
                        } else {
                            resultsArray.add(line.replace(target, replacement));

                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(line)) {
                        if (!TextUtils.isEmpty(target) || !TextUtils.isEmpty(replacement)) {
                            resultSb.append(line.replace(target, replacement));
                        } else {
                            resultSb.append(line);
                        }
                    } else {
                        resultSb.append(line);
                    }
                    resultSb.append("\n");
                }

            }
        } catch (Throwable e) {
            Logs.e(e);
        } finally {
            safeClose(pos, ii, br, is, in, os);
        }

        if (isResultArray) {
            return resultsArray;
        } else {
            return resultSb.toString();
        }
    }

    private static void safeClose(Closeable... obj) {
        if (obj != null && obj.length > 0) {
            for (Closeable close : obj
            ) {
                try {
                    if (close != null)
                        close.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //    public static List<String> getResultArray(String cmd) {
//        List<String> result = new ArrayList<String>();
//        Process proc = null;
//        BufferedInputStream in = null;
//        BufferedReader br = null;
//        InputStreamReader is = null;
//        InputStream ii = null;
//        StringBuilder sb = new StringBuilder();
//        DataOutputStream os = null;
//        OutputStream pos = null;
//        try {
//            proc = Runtime.getRuntime().exec("sh");
//            pos = proc.getOutputStream();
//            os = new DataOutputStream(pos);
//
//            // donnot use os.writeBytes(commmand), avoid chinese charset error
//            os.write(cmd.getBytes());
//            os.writeBytes("\n");
//            os.flush();
//            //exitValue
//            os.writeBytes("exit\n");
//            os.flush();
//            ii = proc.getInputStream();
//            in = new BufferedInputStream(ii);
//            is = new InputStreamReader(in);
//            br = new BufferedReader(is);
//            String line = "";
//            while ((line = br.readLine()) != null) {
//                if (!TextUtils.isEmpty(line) && !result.contains(line)) {
//                    result.add(line);
//                }
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        } finally {
//            safeClose(pos, ii, br, is, in, os);
//        }
//
//        return result;
//    }

//    public static String getResultString(String cmd) {
//        String result = "";
//        Process proc = null;
//        BufferedInputStream in = null;
//        BufferedReader br = null;
//        InputStreamReader is = null;
//        InputStream ii = null;
//        StringBuilder sb = new StringBuilder();
//        DataOutputStream os = null;
//        OutputStream pos = null;
//        try {
//            proc = Runtime.getRuntime().exec("sh");
//            pos = proc.getOutputStream();
//            os = new DataOutputStream(pos);
//
//            // donnot use os.writeBytes(commmand), avoid chinese charset error
//            os.write(cmd.getBytes());
//            os.writeBytes("\n");
//            os.flush();
//            //exitValue
//            os.writeBytes("exit\n");
//            os.flush();
//            ii = proc.getInputStream();
//            in = new BufferedInputStream(ii);
//            is = new InputStreamReader(in);
//            br = new BufferedReader(is);
//            String line = "";
//            while ((line = br.readLine()) != null) {
//                sb.append(line).append("\n");
//            }
//            if (sb.length() > 0) {
//                return sb.substring(0, sb.length() - 1);
//            }
//            result = String.valueOf(sb);
//            if (!TextUtils.isEmpty(result)) {
//                result = result.trim();
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        } finally {
//            safeClose(pos, ii, br, is, in, os);
//        }
//
//        return result;
//    }

}
