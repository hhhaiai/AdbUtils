package me.hhhaiai.adbs;

import me.hhhaiai.adbs.cmds.CmdTools;
import me.hhhaiai.adbs.utils.FileUtils;
import me.hhhaiai.adbs.utils.Logs;
import me.hhhaiai.adbs.utils.StringUtil;
import me.hhhaiai.adbs.utils.android.text.TextUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Adb {

    /**
     * 激活辅助功能
     *
     * @param key
     * @param value
     * @return
     */
    public static String putAccessibility(String key, String value) {
        String cmd = "content call --uri content://settings/secure --method PUT_secure --arg " + key + "  --extra _user:i:0 --extra value:s:" + value;
        return CmdTools.execAdbCmd(cmd, 0);
    }


    public static void main(String[] args) {
        System.out.println("大于29：" + isVersionLargeThan(29));
    }


    /**
     * 真正切换输入法
     *
     * @param ime
     */
    public static void _switchToIme(String ime) {
        CmdTools.execAdbCmd("ime enable " + ime);
        CmdTools.execAdbCmd("ime set " + ime);
    }

    /**
     * 版本大于
     *
     * @param version
     * @return
     */
    public static boolean isVersionLargeThan(int version) {
        String v = StringUtil.trim(CmdTools.execAdbCmd("getprop ro.build.version.sdk"));
        if (!TextUtils.isEmpty(v)) {
            return Integer.parseInt(v) > version;
        }
        return false;
    }

    /**
     * 读取外部ADB KEY信息
     */
    public static boolean readOuterAdbKey(File privKey, File pubKey) {

        try {
            String content = FileUtils.readFile(privKey);
            content = content.replace("-----BEGIN PRIVATE KEY-----\n", "");
            content = content.replace("-----END PRIVATE KEY-----", "");
//            byte[] decoded = Base64.decode(content, Base64.DEFAULT);
            byte[] decoded = Base64.getDecoder().decode(content);
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(decoded);
            File targetFile = new File(MemoryContent.KEY_PATH_PRIVATE);
            FileOutputStream stream = new FileOutputStream(targetFile);
            stream.write(encodedKeySpec.getEncoded());
            stream.flush();
            stream.close();
        } catch (Exception e) {
            Logs.em("Copy Private Key failed", e);
            return false;
        }

        try {
            String content = FileUtils.readFile(pubKey);
            if (StringUtil.isEmpty(content)) {
                Logs.e("Public Key为空");
                return false;
            }

            PublicKey publicKey = parseAndroidPubKey(content);

            File targetFile = new File(MemoryContent.KEY_PATH_PUBLIC);
            FileOutputStream stream = new FileOutputStream(targetFile);
            stream.write(publicKey.getEncoded());
            stream.flush();
            stream.close();
        } catch (Exception e) {
            Logs.e(e);
            return false;
        }

        return true;
    }

    /**
     * parse android public key
     *
     * @param inputKey
     * @return
     */
    public static PublicKey parseAndroidPubKey(String inputKey) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(inputKey));
        String line = null;
        try {
            line = bufferedReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        line = line.replaceAll(" .*@.*", "");
        byte[] raw = Base64.getDecoder().decode(line);
//        byte[] raw = Base64.decode(line, Base64.NO_WRAP);
        ByteBuffer bb = ByteBuffer.wrap(raw);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer intBuffer = bb.asIntBuffer();
        int len = intBuffer.get();
        BigInteger n0Inv = BigInteger.valueOf(intBuffer.get());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(len * 4);
        int[] dst = new int[len];
        intBuffer.get(dst);
        reverse(dst);
        for (int i = 0; i < len; i++) {
            int value = dst[i];
            byte[] convertedBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
            byteArrayOutputStream.write(convertedBytes, 0, convertedBytes.length);
        }
        byte[] n = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.reset();
        dst = new int[len];
        intBuffer.get(dst);
        reverse(dst);
        for (int i = 0; i < len; i++) {
            int value = dst[i];
            byte[] convertedBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
            byteArrayOutputStream.write(convertedBytes, 0, convertedBytes.length);
        }
        int e = intBuffer.get();

        RSAPublicKey publicKey;
        try {
            publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(1, n), BigInteger.valueOf(e)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return publicKey;
    }

    /**
     * <p>Reverses the order of the given array.</p>
     *
     * <p>This method does nothing for a {@code null} input array.</p>
     *
     * @param array the array to reverse, may be {@code null}
     */
    public static void reverse(final int[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * <p>
     * Reverses the order of the given array in the given range.
     * </p>
     *
     * <p>
     * This method does nothing for a {@code null} input array.
     * </p>
     *
     * @param array               the array to reverse, may be {@code null}
     * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
     *                            change.
     * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
     *                            change. Overvalue (&gt;array.length) is demoted to array length.
     * @since 3.2
     */
    public static void reverse(final int[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = startIndexInclusive < 0 ? 0 : startIndexInclusive;
        int j = Math.min(array.length, endIndexExclusive) - 1;
        int tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * 获取顶部包名和activity
     *
     * @return
     */
    public static String[] getTopPkgAndActivity() {
        // Build.VERSION.SDK_INT >= 29
        if (isVersionLargeThan(29)) {
            String result = CmdTools.execAdbCmd("dumpsys window visible-apps | grep \"mCurrentFocus\"");
            if (TextUtils.isEmpty(result)) {
                return null;
            }
            result = result.trim();

            // 目标区分
            String[] split = result.split("\\s+");
            if (split.length < 3) {
                return null;
            }
            String[] pA = split[split.length - 1].split("/");
            if (pA.length != 2) {
                return null;
            }

            // .开头优化
            if (pA[1].startsWith(".")) {
                pA[1] = pA[0] + pA[1];
            }
            if (pA[1].contains("}")) {
                pA[1] = pA[1].split("\\}")[0];
            }
            Logs.i("Get top pkg and activity::" + Arrays.toString(pA));
            return pA;
        } else {
            String result = CmdTools.execAdbCmd("dumpsys activity activities | grep 'Running' -A3 | grep 'Run #'");
            if (TextUtils.isEmpty(result)) {
                return null;
            }
            result = result.trim();

            // 目标区分
            String target = result.split("\n")[0].trim();
            String[] split = target.split("\\s+");
            if (split.length < 5) {
                return null;
            }
            String[] pA = split[split.length - 2].split("/");
            if (pA.length != 2) {
                return null;
            }

            // .开头优化
            if (pA[1].startsWith(".")) {
                pA[1] = pA[0] + pA[1];
            }
            Logs.i("Get top pkg and activity::" + Arrays.toString(pA));
            return pA;
        }
    }
//    /**
//     * 快捷执行ps指令
//     * @param filter grep 过滤条件
//     * @return 分行结果
//     */
//    public static String[] ps(String filter) {
//        try {
//            if (!RomUtils.isOppoSystem() && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
//                try {
//                    Process p;
//                    if (filter != null && filter.length() > 0) {
//                        p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "ps | grep '" + filter + "'"});
//                    } else {
//                        p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "ps"});
//                    }
//                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                    String line;
//                    List<String> results = new ArrayList<>();
//                    while ((line = br.readLine()) != null) {
////            		LogUtil.d(TAG, "ERR************" + line);
//                        results.add(line);
//                    }
//                    return results.toArray(new String[results.size()]);
//                } catch (IOException e) {
//                    LogUtil.e(TAG, "Read ps content failed", e);
//                    return new String[0];
//                }
//            } else if (Build.VERSION.SDK_INT <= 25) {
//                // Android 7.0, 7.1无法通过应用权限获取所有进程
//                if (isRooted()) {
//                    if (filter != null && filter.length() > 0) {
//                        return execRootCmd("ps | grep '" + filter + "'", null, true, null).toString().split("\n");
//                    } else {
//                        return execRootCmd("ps", null, true, null).toString().split("\n");
//                    }
//                } else {
//                    if (filter != null && filter.length() > 0) {
//
//                        // 存在ps命令调用超时情况
//                        return execAdbCmd("ps | grep '" + filter + "'", 2500).split("\n");
//                    } else {
//                        return execAdbCmd("ps", 2500).split("\n");
//                    }
//                }
//            } else {
//                String[] result;
//                // Android O ps为toybox实现，功能与标准ps命令基本相同，需要-A参数获取全部进程
//                if (isRooted()) {
//                    if (filter != null && filter.length() > 0) {
//                        result = execRootCmd("ps -ef | grep '" + filter + "'", null, true, null).toString().split("\n");
//                    } else {
//                        result = execRootCmd("ps -ef", null, true, null).toString().split("\n");
//                    }
//                } else {
//                    if (filter != null && filter.length() > 0) {
//
//                        // 存在ps命令调用超时情况
//                        result = execAdbCmd("ps -ef | grep '" + filter + "'", 2500).split("\n");
//                    } else {
//                        result = execAdbCmd("ps -ef", 2500).split("\n");
//                    }
//                }
//
//                if (StringUtil.isEmpty(filter)) {
//                    return result;
//                }
//
//                // 过滤 grep XXXX 的内容
//                ArrayList<String> filtered = new ArrayList<>(result.length);
//                for (String line : result) {
//                    if (StringUtil.contains(line, "grep " + filter)) {
//                        continue;
//                    } else if (StringUtil.contains(line, "grep '" + filter)) {
//                        continue;
//                    }
//                    filtered.add(line);
//                }
//
//                return filtered.toArray(new String[]{});
//            }
//        } catch (Exception e) {
//            LogUtil.e(TAG, "Fail to execute ps func", e);
//            return new String[0];
//        }
//    }
}
