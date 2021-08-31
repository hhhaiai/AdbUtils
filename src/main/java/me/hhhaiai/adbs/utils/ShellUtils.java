package me.hhhaiai.adbs.utils;

import me.hhhaiai.adbs.utils.android.text.TextUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShellUtils {
    public static List<String> getResultArray(String cmd) {
        List<String> result = new ArrayList<String>();
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        StringBuilder sb = new StringBuilder();
        DataOutputStream os = null;
        OutputStream pos = null;
        try {
            proc = Runtime.getRuntime().exec("sh");
            pos = proc.getOutputStream();
            os = new DataOutputStream(pos);

            // donnot use os.writeBytes(commmand), avoid chinese charset error
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.flush();
            //exitValue
            os.writeBytes("exit\n");
            os.flush();
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!TextUtils.isEmpty(line) && !result.contains(line)) {
                    result.add(line);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            safeClose(pos, ii, br, is, in, os);
        }

        return result;
    }

    public static String getResultString(String cmd) {
        String result = "";
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        StringBuilder sb = new StringBuilder();
        DataOutputStream os = null;
        OutputStream pos = null;
        try {
            proc = Runtime.getRuntime().exec("sh");
            pos = proc.getOutputStream();
            os = new DataOutputStream(pos);

            // donnot use os.writeBytes(commmand), avoid chinese charset error
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.flush();
            //exitValue
            os.writeBytes("exit\n");
            os.flush();
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            if (sb.length() > 0) {
                return sb.substring(0, sb.length() - 1);
            }
            result = String.valueOf(sb);
            if (!TextUtils.isEmpty(result)) {
                result = result.trim();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            safeClose(pos, ii, br, is, in, os);
        }

        return result;
    }

    public static String getStringUseAdbShell(String cmd) {
        String result = "";
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        StringBuilder sb = new StringBuilder();
        DataOutputStream os = null;
        OutputStream pos = null;
        try {
            proc = Runtime.getRuntime().exec("adb shell");
            pos = proc.getOutputStream();
            os = new DataOutputStream(pos);

            // donnot use os.writeBytes(commmand), avoid chinese charset error
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.flush();
            //exitValue
            os.writeBytes("exit\n");
            os.flush();
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            if (sb.length() > 0) {
                return sb.substring(0, sb.length() - 1);
            }
            result = String.valueOf(sb);
            if (!TextUtils.isEmpty(result)) {
                result = result.trim();
            }
        } catch (Throwable e) {
        } finally {
            safeClose(pos, ii, br, is, in, os);
        }

        return result;
    }


    public static CopyOnWriteArrayList<String> getArrayUseAdbShell(String cmd) {
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        StringBuilder sb = new StringBuilder();
        DataOutputStream os = null;
        OutputStream pos = null;
        CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();
        try {
            proc = Runtime.getRuntime().exec("adb shell");
            pos = proc.getOutputStream();
            os = new DataOutputStream(pos);

            // donnot use os.writeBytes(commmand), avoid chinese charset error
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.flush();
            //exitValue
            os.writeBytes("exit\n");
            os.flush();
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!TextUtils.isEmpty(line)) {
                    results.add(line);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            safeClose(pos, ii, br, is, in, os);
        }

        return results;
    }

    public static CopyOnWriteArrayList<String> getArrayUseAdbShell(String cmd, String replaceBeforeStr, String replaceAfterStr) {
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        StringBuilder sb = new StringBuilder();
        DataOutputStream os = null;
        OutputStream pos = null;
        CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();
        try {
            proc = Runtime.getRuntime().exec("adb shell");
            pos = proc.getOutputStream();
            os = new DataOutputStream(pos);

            // donnot use os.writeBytes(commmand), avoid chinese charset error
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.flush();
            //exitValue
            os.writeBytes("exit\n");
            os.flush();
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!TextUtils.isEmpty(line)) {
                    results.add(line.replace(String.valueOf(replaceBeforeStr), String.valueOf(replaceAfterStr)));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            safeClose(pos, ii, br, is, in, os);
        }

        return results;
    }


    public static CopyOnWriteArrayList<String> getArrayUseAdb(String cmd) {
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        OutputStream pos = null;
        CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();
        try {
            proc = Runtime.getRuntime().exec(cmd);
            pos = proc.getOutputStream();
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!TextUtils.isEmpty(line)) {
                    results.add(line);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            safeClose(pos, ii, br, is, in);
        }

        return results;
    }

    private static void safeClose(Closeable... obj) {
        if (obj != null && obj.length > 0) {
            for (Closeable close : obj
            ) {

                try {
                    close.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }


    }

}
