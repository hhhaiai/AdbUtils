package me.hhhaiai.adbs.cmds;


import com.cgutman.adblib.AdbStream;
import com.cgutman.adblib.ByteQueueInputStream;
import me.hhhaiai.adbs.interfaces.AbstCmdLine;
import me.hhhaiai.adbs.interfaces.WrapSocket;
import me.hhhaiai.adbs.utils.Logs;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * 命令行操作包装
 * Created by cathor on 2017/12/26.
 */
public class CmdLine implements AbstCmdLine, WrapSocket {
    private static final String TAG = "CmdLine";

    protected String cmdTag;

    /**
     * 包装的adbStream
     */
    private AdbStream stream;

    /**
     * 包装的process
     */
    private Process suProcess;

    /**
     * 重定向输出
     */
    private ConcurrentLinkedQueue<String> redirectLog;

    /**
     * Process reader
     */
    private InputStream inputStream;

    private ExecutorService singleTaskExecutor;

    private Queue<byte[]> readList;

    /**
     * 是否是adb模式
     */
    private boolean isAdb;

    protected CmdLine(AdbStream stream) {
        isAdb = true;
        this.stream = stream;
        this.suProcess = null;
        this.inputStream = stream.getInputStream();
    }

    protected CmdLine(Process suProcess) {
        this.isAdb = false;
        this.stream = null;
        this.suProcess = suProcess;

        // 对于Process，需要一个独立的线程来读取命令行输出
        this.inputStream = suProcess.getInputStream();
        this.readList = new LinkedBlockingQueue<>();
        singleTaskExecutor = Executors.newSingleThreadExecutor();
        singleTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    while ((length = inputStream.read(bytes, 0, 1024)) > 0) {
                        readList.add(Arrays.copyOf(bytes, length));
                    }
                } catch (IOException e) {
                    Logs.e(e);
                }
            }
        });
    }

    /**
     * 向命令行执行写操作
     *
     * @param cmd
     * @throws Exception
     */
    public void writeCommand(String cmd) {
        if (cmd == null) {
            cmd = "";
        }
        try {
            CmdTools.logcatCmd(cmdTag + cmd);
            if (!cmd.endsWith("\n")) {
                cmd = cmd + "\n";
            }
            if (isAdb) {
                stream.write(cmd);
            } else {
                DataOutputStream stream = new DataOutputStream(suProcess.getOutputStream());
                stream.writeBytes(cmd);
                stream.flush();
            }
        } catch (Exception e) {
            if (cmd.length() > 100) {
                Logs.em("Write command " + cmd.substring(0, 100) + "... failed", e);
            } else {
                Logs.em("Write command " + cmd + "... failed", e);
            }
        }
    }

    /**
     * 读取命令行当前所有输出
     *
     * @return
     */
    public String readUntilSomething() {
        try {
            if (isAdb) {
                BlockingQueue<byte[]> queue = (BlockingQueue<byte[]>) stream.getReadQueue();
                StringBuilder builder = new StringBuilder();
                // 读取消息
                synchronized (stream.getReadQueue()) {
                    String content = new String(queue.take());
                    builder.append(content);
                    while (!queue.isEmpty()) {
                        content = new String(queue.poll());
                        builder.append(content);
                    }
                    stream.getReadQueue().notifyAll();
                }
                return builder.toString();
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                while (!readList.isEmpty()) {
                    stringBuilder.append(new String(readList.poll()));
                }

                return stringBuilder.toString();
            }
        } catch (Exception e) {
            Logs.e(e);
            return null;
        }
    }

    /**
     * 读取命令行当前所有输出
     *
     * @return
     */
    public String readOutput() {
        try {
            if (isAdb) {
                Queue<byte[]> queue = stream.getReadQueue();
                StringBuilder builder = new StringBuilder();
                // 读取消息
                synchronized (stream.getReadQueue()) {
                    while (!queue.isEmpty()) {
                        String content = new String(queue.poll());
                        builder.append(content);
                    }
                    stream.getReadQueue().notifyAll();
                }
                return builder.toString();
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                while (!readList.isEmpty()) {
                    stringBuilder.append(new String(readList.poll()));
                }

                return stringBuilder.toString();
            }
        } catch (Exception e) {
            Logs.e(e);
            return null;
        }
    }

    @Override
    public void writeBytes(byte[] content) {
        try {
            if (isAdb) {
                stream.write(content, true);
            } else {
                OutputStream outputStream = suProcess.getOutputStream();
                outputStream.write(content);
                outputStream.flush();
            }
        } catch (IOException e) {
            Logs.e(e);
        } catch (InterruptedException e) {
            Logs.e(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * 强制关闭命令行
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (isAdb) {
            stream.close();
        } else {
            suProcess.destroy();
        }
    }

    /**
     * 命令行是否关闭
     *
     * @return
     */
    public boolean isClosed() {
        if (isAdb) {
            return stream.isClosed();
        } else {
            try {
                int exit = suProcess.exitValue();
                return true;
            } catch (IllegalThreadStateException e) {
                return false;
            }
        }
    }

    @Override
    public void disconnect() {
        ((ByteQueueInputStream) inputStream).closeSocketForwardingMode();
        Logs.i("Wrap Connection disconnect");
    }

    /**
     * 获取命令行读取器
     *
     * @return
     */
    public CmdLineReader getReader() {
        return new CmdLineReader(this);
    }

    public String getCmdTag() {
        return cmdTag;
    }

    public static class CmdLineReader {
        private CmdLine cmdLine;
        private Queue<String> outputQueue;
        private int curPos;

        private CmdLineReader(CmdLine cmdLine) {
            this.cmdLine = cmdLine;
            outputQueue = new LinkedList<>();
            curPos = 0;
        }

        public String readLine() {
            StringBuilder buffer = new StringBuilder();
            boolean eol = false;
            while (!eol) {
                if (outputQueue.isEmpty()) {
                    String newContent = cmdLine.readUntilSomething();
                    if (newContent == null) {
                        return buffer.toString();
                    }
                    outputQueue.add(newContent);
                    curPos = 0;
                }

                String first = outputQueue.peek();
                if (first == null) {
                    continue;
                }
                int nextEol = -1;
                if ((nextEol = first.indexOf('\n', curPos)) < 0) {
                    buffer.append(first, curPos, first.length());
                    outputQueue.poll();
                    curPos = 0;
                } else {
                    buffer.append(first, curPos, nextEol);
                    eol = true;
                    curPos = nextEol + 1;
                }
            }

            return buffer.toString();
        }
    }
}
