package me.hhhaiai.adbs.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logs {
    private static final String TAG = "sanbo";

    public static void d(String info) {
        d(TAG, info);
    }

    public static void d(Throwable th) {
        d(TAG, null, th);
    }

    public static void d(String tag, String info) {
        d(tag, info, null);
    }

    public static void d(String tag, String info, Throwable e) {
        if (e != null) {
            System.out.println(String.format("D[%s]  %s\r\n %s", tag, info, getStackTraceString(e)));
        } else {
            System.out.println(String.format("D[%s]  %s", tag, info));
        }
    }

    /***********************************************/
    public static void i(String info) {
        i(TAG, info);
    }

    public static void i(Throwable th) {
        i(TAG, null, th);
    }

    public static void i(String tag, String info) {
        i(tag, info, null);
    }

    public static void i(String tag, String info, Throwable e) {

        if (e != null) {
            System.out.println(String.format("I[%s]  %s\r\n %s", tag, info, getStackTraceString(e)));
        } else {
            System.out.println(String.format("I[%s]  %s", tag, info));
        }
    }


    /***********************************************/
    public static void e(String info) {
        e(TAG, info);
    }

    public static void e(Throwable th) {
        e(TAG, null, th);
    }

    public static void e(String tag, String info) {
        e(tag, info, null);
    }

    public static void em(String msg, Throwable e) {
        e(TAG, msg, e);
    }

    public static void e(String tag, String info, Throwable e) {

        if (e != null) {
            System.err.println(String.format("E[%s]  %s\r\n %s", tag, info, getStackTraceString(e)));
        } else {
            System.err.println(String.format("E[%s]  %s", tag, info));
        }
    }

    /***********************************************/
    public static void w(String info) {
        w(TAG, info);
    }

    public static void w(Throwable th) {
        w(TAG, null, th);
    }

    public static void w(String tag, String info) {
        w(tag, info, null);
    }

    public static void w(String tag, String info, Throwable e) {

        if (e != null) {
            System.err.println(String.format("W[%s]  %s\r\n %s", tag, info, getStackTraceString(e)));
        } else {
            System.err.println(String.format("W[%s]  %s", tag, info));
        }
    }

    /***********************************************/
    public static void wtf(String info) {
        wtf(TAG, info);
    }

    public static void wtf(Throwable th) {
        wtf(TAG, null, th);
    }

    public static void wtf(String tag, String info) {
        wtf(tag, info, null);
    }

    public static void wtf(String tag, String info, Throwable e) {
        if (e != null) {
            System.err.println(String.format("WTF[%s]  %s\r\n %s", tag, info, getStackTraceString(e)));
        } else {
            System.err.println(String.format("WTF[%s]  %s", tag, info));
        }
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }
}
