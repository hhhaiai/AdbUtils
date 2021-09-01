package com.mrkid.flashbot;


import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbStream;
import me.hhhaiai.adbs.utils.Logs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by xudong on 2/25/14.
 */
public class Push {

    private AdbConnection adbConnection;
    private File local;
    private String remotePath;

    public Push(AdbConnection adbConnection, File local, String remotePath) {
        this.adbConnection = adbConnection;
        this.local = local;
        this.remotePath = remotePath;
    }

    public void execute() throws Exception {

        AdbStream stream = adbConnection.open("sync:");

        String sendId = "SEND";

        String mode = ",33206";

        int length = (remotePath + mode).length();

        stream.write(ByteUtils.concat(sendId.getBytes(), ByteUtils.intToByteArray(length)));

        stream.write(remotePath.getBytes());

        stream.write(mode.getBytes());

        byte[] buff = new byte[adbConnection.getMaxData()];
        InputStream is = new FileInputStream(local);

        long sent = 0;
        long total = local.length();
        int lastProgress = 0;
        while (true) {
            int read = is.read(buff);
            if (read < 0) {
                break;
            }
            stream.write(ByteUtils.concat("DATA".getBytes(), ByteUtils.intToByteArray(read)));
            if (read == buff.length) {
                stream.write(buff);
            } else {
                byte[] tmp = new byte[read];
                System.arraycopy(buff, 0, tmp, 0, read);
                stream.write(tmp);
            }

            sent += read;

//            // 进度打印
//            final int progress = (int)(sent * 100 / total);
//            if (lastProgress != progress) {
//                System.out.println(progress);
//                lastProgress = progress;
//            }
        }

        stream.write(ByteUtils.concat("DONE".getBytes(), ByteUtils.intToByteArray((int) System.currentTimeMillis())));

        byte[] res = stream.read();
        // TODO: test if res contains "OKEY" or "FAIL"
        Logs.d(new String(res));

        stream.write(ByteUtils.concat("QUIT".getBytes(), ByteUtils.intToByteArray(0)));
    }
}
